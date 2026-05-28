package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Examination;
import com.example.keshe1.entity.ExaminationItem;
import com.example.keshe1.entity.MedicalRecord;
import com.example.keshe1.mapper.ExaminationItemMapper;
import com.example.keshe1.mapper.ExaminationMapper;
import com.example.keshe1.mapper.MedicalRecordMapper;
import com.example.keshe1.service.ExaminationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExaminationServiceImpl extends ServiceImpl<ExaminationMapper, Examination> implements ExaminationService {

    @Autowired
    private ExaminationItemMapper examinationItemMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    /**
     * 获取所有详情（管理员或医生列表用）
     * 修改：按 ID 降序排列 (最新开的单在最上面)
     */
    @Override
    public List<Examination> getAllDetails() {
        QueryWrapper<Examination> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("e.id");
        return this.baseMapper.selectExaminationDetails(wrapper);
    }

    /**
     * 按患者ID查询
     * 修改：按 ID 降序排列
     */
    @Override
    public List<Examination> getByPatientId(Long patientId) {
        QueryWrapper<Examination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("e.patient_id", patientId);
        queryWrapper.orderByDesc("e.id");
        return this.baseMapper.selectExaminationDetails(queryWrapper);
    }

    /**
     * 按病历ID查询
     * 修改：按 ID 降序排列
     */
    @Override
    public List<Examination> getByRecordId(Long recordId) {
        QueryWrapper<Examination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("e.record_id", recordId);
        queryWrapper.orderByDesc("e.id");
        return this.baseMapper.selectExaminationDetails(queryWrapper);
    }

    @Override
    public int countByPatientId(Long patientId) {
        QueryWrapper<Examination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        return (int) this.count(queryWrapper);
    }

    @Override
    public List<ExaminationItem> getAllExaminationItems() {
        return examinationItemMapper.selectList(null);
    }

    /**
     * 开具检查
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void prescribeExaminations(Long recordId, List<Long> itemIds) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) throw new RuntimeException("病历不存在");

        // 1. 获取该病历已经开过的检查项目ID
        QueryWrapper<Examination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_id", recordId);
        List<Examination> existingExams = this.list(queryWrapper);

        List<Long> existingItemIds = existingExams.stream()
                .map(Examination::getExaminationItemId)
                .collect(Collectors.toList());

        boolean hasChanges = false;

        // 2. 处理要删除的项目（在现有但不在提交的列表中）
        for (Examination existingExam : existingExams) {
            if (!itemIds.contains(existingExam.getExaminationItemId())) {
                // 检查是否已完成，已完成的不能删除
                if ("已完成".equals(existingExam.getStatus())) {
                    continue; // 跳过已完成的检查
                }
                // 删除项目
                this.removeById(existingExam.getId());
                hasChanges = true;
            }
        }

        // 3. 处理要新增的项目
        for (Long itemId : itemIds) {
            if (!existingItemIds.contains(itemId)) {
                ExaminationItem item = examinationItemMapper.selectById(itemId);
                if (item == null) continue;

                Examination examination = new Examination();
                examination.setRecordId(recordId);
                examination.setPatientId(record.getPatientId());
                examination.setDoctorId(record.getDoctorId());
                examination.setExaminationItemId(itemId);
                examination.setExaminationDate(record.getVisitDate() != null ? record.getVisitDate() : LocalDate.now());
                examination.setStatus("待完成");

                this.save(examination);
                hasChanges = true;
            }
        }

        // 4. 强制刷新病历文本，无论是否有变化
        // 这是关键：确保每次点击确认开单都重新生成病历文本
        refreshMedicalRecordText(recordId);
    }

    /**
     * 填写结果
     * 逻辑更新：保存结果后，调用 refreshMedicalRecordText 统一更新病历文本
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillExaminationResult(Long examinationId, String result) {
        // 1. 获取检查单详情
        QueryWrapper<Examination> wrapper = new QueryWrapper<>();
        wrapper.eq("e.id", examinationId);
        List<Examination> list = this.baseMapper.selectExaminationDetails(wrapper);

        if (list.isEmpty()) throw new RuntimeException("检查单不存在");
        Examination exam = list.get(0);

        // 2. 更新状态和结果
        exam.setExaminationResult(result);
        exam.setStatus("已完成");
        this.updateById(exam);

        // 3. 更新完结果，重新刷新病历表中的辅助检查文本
        if (exam.getRecordId() != null) {
            refreshMedicalRecordText(exam.getRecordId());
        }
    }

    /**
     * 重新生成病历中的辅助检查文本
     */
    private void refreshMedicalRecordText(Long recordId) {
        // 1. 查询该病历下所有的检查单
        QueryWrapper<Examination> wrapper = new QueryWrapper<>();
        wrapper.eq("e.record_id", recordId);
        wrapper.orderByAsc("e.id");
        List<Examination> exams = this.baseMapper.selectExaminationDetails(wrapper);

        // 如果没有任何检查项目，将辅助检查设为空字符串
        if (exams.isEmpty()) {
            MedicalRecord record = new MedicalRecord();
            record.setId(recordId);
            record.setAuxiliaryExamination(""); // 设为空字符串
            medicalRecordMapper.updateById(record);
            return;
        }

        StringBuilder sb = new StringBuilder();
        int index = 1;

        // 2. 遍历拼接字符串
        for (Examination exam : exams) {
            String itemName = exam.getExaminationItemName();
            if (itemName == null) itemName = "未知项目";

            String resultText = exam.getExaminationResult();
            if (resultText == null || resultText.trim().isEmpty()) {
                resultText = "待完成";
            }

            sb.append(index).append(". ")
                    .append(itemName).append(": ")
                    .append(resultText)
                    .append("\n");

            index++;
        }

        // 3. 更新病历表
        MedicalRecord record = new MedicalRecord();
        record.setId(recordId);
        record.setAuxiliaryExamination(sb.toString().trim());
        medicalRecordMapper.updateById(record);
    }

    // 在 ExaminationServiceImpl 类中添加以下方法：

    /**
     * 根据状态筛选检查项目详情
     * 新增方法：支持状态筛选，按 ID 降序排列
     */
    @Override
    public List<Examination> getDetailsByStatus(String status) {
        QueryWrapper<Examination> wrapper = new QueryWrapper<>();

        // 如果状态不为空，添加状态条件
        if (status != null && !status.trim().isEmpty()) {
            if ("已完成".equals(status)) {
                wrapper.eq("e.status", "已完成");
            } else if ("待完成".equals(status)) {
                wrapper.eq("e.status", "待完成");
            } else if ("未完成".equals(status)) {
                wrapper.ne("e.status", "已完成"); // 或者根据你的业务逻辑
            }
        }

        wrapper.orderByDesc("e.id");
        return this.baseMapper.selectExaminationDetails(wrapper);
    }

    /**
     * 删除检查项目
     * 1. 检查状态，已完成不可删
     * 2. 删除数据
     * 3. 重新刷新病历文本（序号会自动前移）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeExamination(Long id) {
        Examination exam = this.getById(id);
        if (exam == null) {
            return; // 或抛异常
        }

        // 状态保护
        if ("已完成".equals(exam.getStatus())) {
            throw new RuntimeException("该检查已出结果，无法删除！");
        }

        // 执行删除
        this.removeById(id);

        // 刷新病历显示 (例如 1,2,3 删了2，自动变 1,2)
        if (exam.getRecordId() != null) {
            refreshMedicalRecordText(exam.getRecordId());
        }
    }

    /**
     * 根据ID获取检查项目
     */
    @Override
    public ExaminationItem getExaminationItemById(Long itemId) {
        return examinationItemMapper.selectById(itemId);
    }
}

