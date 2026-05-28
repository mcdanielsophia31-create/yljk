package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Examination;
import com.example.keshe1.entity.ExaminationItem; // 确保引入

import java.util.List;

public interface ExaminationService extends IService<Examination> {

    List<Examination> getByPatientId(Long patientId);
    List<Examination> getByRecordId(Long recordId);
    int countByPatientId(Long patientId);

    /**
     * 获取所有检查项目配置（用于下拉框）
     */
    List<ExaminationItem> getAllExaminationItems();

    /**
     * 获取所有检查报告详情（包含项目名称等）
     */
    List<Examination> getAllDetails();

    List<Examination> getDetailsByStatus(String status);

    /**
     * 医生开具检查
     */
    void prescribeExaminations(Long recordId, List<Long> itemIds);

    /**
     * 填写检查结果
     */
    void fillExaminationResult(Long examinationId, String result);

    /**
     * 删除检查项目
     */
    void removeExamination(Long id);

    /**
     * 根据ID获取检查项目
     */
    ExaminationItem getExaminationItemById(Long itemId);
}