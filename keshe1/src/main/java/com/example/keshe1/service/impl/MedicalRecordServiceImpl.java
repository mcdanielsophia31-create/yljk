package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.dto.HospitalMedicalRecordDTO;
import com.example.keshe1.dto.MedicalRecordPriceDTO;
import com.example.keshe1.dto.PrescriptionItemDTO;
import com.example.keshe1.entity.*;
import com.example.keshe1.mapper.MedicalRecordMapper;
import com.example.keshe1.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ObjectMapper objectMapper;

    // ================== 病历基础操作 ==================

    @Override
    public List<MedicalRecord> listByPatientId(Long patientId) {
        QueryWrapper<MedicalRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.orderByDesc("visit_date");
        List<MedicalRecord> records = this.list(queryWrapper);
        System.out.println("根据患者ID " + patientId + " 查询到 " + records.size() + " 条病历记录");
        return records;
    }

    @Override
    public List<MedicalRecord> getByDoctorId(Long doctorId) {
        QueryWrapper<MedicalRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        return list(queryWrapper);
    }

    @Override
    public int countByPatientId(Long patientId) {
        // 获取该患者的病历记录
        List<MedicalRecord> records = listByPatientId(patientId);

        // 如果病历为空，返回0
        if (records == null || records.isEmpty()) {
            return 0;
        }

        int medicationCount = 0;

        // 遍历每个病历，解析treatmentPlan中的用药记录
        for (MedicalRecord record : records) {
            String treatmentPlan = record.getTreatmentPlan();
            if (treatmentPlan != null && !treatmentPlan.trim().isEmpty()) {
                try {
                    // 尝试解析JSON格式的处方
                    if (treatmentPlan.trim().startsWith("[")) {
                        List<PrescriptionItemDTO> medicines = objectMapper.readValue(
                                treatmentPlan,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, PrescriptionItemDTO.class)
                        );

                        // 统计该病历中的用药数量
                        if (medicines != null) {
                            medicationCount += medicines.size();
                        }
                    } else {
                        // 如果不是JSON格式，可能是一个简单的文本，计数为1
                        medicationCount += 1;
                    }
                } catch (Exception e) {
                    // 解析失败，记录日志但不影响其他病历
                    System.err.println("解析治疗方案JSON失败: " + e.getMessage());
                    // 如果解析失败，但仍然有内容，计数为1
                    medicationCount += 1;
                }
            }
        }

        System.out.println("患者ID " + patientId + " 的用药记录数量: " + medicationCount);
        return medicationCount;
    }

    @Override
    public List<HospitalMedicalRecordDTO> getHospitalMedicalRecordsByDoctorId(Long doctorId) {
        try {
            String sql = "CALL GetHospitalMedicalRecords(?)";
            List<HospitalMedicalRecordDTO> results = jdbcTemplate.query(
                    sql,
                    new Object[]{doctorId},
                    new BeanPropertyRowMapper<>(HospitalMedicalRecordDTO.class)
            );
            return results.stream()
                    .filter(dto -> dto.getRecordId() != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("调用存储过程失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<HospitalMedicalRecordDTO> searchMedicalRecords(Long doctorId, String keyword) {
        List<HospitalMedicalRecordDTO> allRecords = getHospitalMedicalRecordsByDoctorId(doctorId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return allRecords;
        }
        String searchKey = keyword.trim().toLowerCase();
        return allRecords.stream()
                .filter(record -> record.getPatientName() != null &&
                        record.getPatientName().toLowerCase().contains(searchKey))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicalRecord createMedicalRecord(Long doctorId, Long patientId) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatientId(patientId);
        medicalRecord.setDoctorId(doctorId);
        medicalRecord.setVisitDate(LocalDate.now());
        medicalRecord.setCreatedTime(LocalDateTime.now());
        this.save(medicalRecord);
        return medicalRecord;
    }

    @Override
    @Transactional
    public boolean updateTreatmentPlan(Long recordId, String treatmentPlan) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(recordId);
        medicalRecord.setTreatmentPlan(treatmentPlan);
        medicalRecord.setUpdatedTime(LocalDateTime.now());
        return this.updateById(medicalRecord);
    }

    // ================== 病历总金额计算 ==================

    @Override
    @Transactional
    public MedicalRecordPriceDTO calculateTotalPrice(Long recordId) {
        MedicalRecordPriceDTO priceDTO = new MedicalRecordPriceDTO();
        priceDTO.setRecordId(recordId);

        BigDecimal totalPrice = BigDecimal.ZERO;

        // 1. 获取病历信息
        MedicalRecord record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("病历不存在");
        }

        // 2. 计算挂号费（从医生表获取）
        Doctor doctor = doctorService.getById(record.getDoctorId());
        if (doctor != null) {
            BigDecimal registrationFee = doctor.getRegistrationFeeByTitle();
            priceDTO.setRegistrationFee(registrationFee);
            totalPrice = totalPrice.add(registrationFee);
        }

        // 3. 计算药费（解析treatmentPlan字段）
        BigDecimal medicineFee = BigDecimal.ZERO;
        String treatmentPlan = record.getTreatmentPlan();
        if (treatmentPlan != null && !treatmentPlan.trim().isEmpty()) {
            try {
                // 解析JSON格式的处方
                if (treatmentPlan.trim().startsWith("[")) {
                    List<PrescriptionItemDTO> medicines = objectMapper.readValue(
                            treatmentPlan,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, PrescriptionItemDTO.class)
                    );

                    for (PrescriptionItemDTO medicine : medicines) {
                        if (medicine.getMedicineId() != null) {
                            // 从数据库获取药品单价
                            Medicine med = medicineService.getById(medicine.getMedicineId());
                            if (med != null && med.getPrice() != null) {
                                // 计算药费：单价 × 开药数量（盒）
                                BigDecimal quantity = BigDecimal.valueOf(medicine.getTotalQuantity() != null ?
                                        medicine.getTotalQuantity() : 1);
                                BigDecimal itemFee = med.getPrice().multiply(quantity);
                                medicineFee = medicineFee.add(itemFee);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 解析失败，药费为0
                e.printStackTrace();
            }
        }
        priceDTO.setMedicineFee(medicineFee);
        totalPrice = totalPrice.add(medicineFee);

        // 4. 计算检查费
        BigDecimal examinationFee = BigDecimal.ZERO;
        List<Examination> examinations = examinationService.getByRecordId(recordId);
        for (Examination exam : examinations) {
            if (exam.getExaminationItemId() != null) {
                // 从检查项目表获取价格
                ExaminationItem item = examinationService.getExaminationItemById(exam.getExaminationItemId());
                if (item != null && item.getPrice() != null) {
                    examinationFee = examinationFee.add(item.getPrice());
                }
            }
        }
        priceDTO.setExaminationFee(examinationFee);
        totalPrice = totalPrice.add(examinationFee);

        priceDTO.setTotalPrice(totalPrice);

        return priceDTO;
    }

    @Override
    @Transactional
    public boolean updateTotalPrice(Long recordId) {
        try {
            // 计算总金额
            MedicalRecordPriceDTO priceDTO = calculateTotalPrice(recordId);

            // 更新到数据库
            MedicalRecord record = new MedicalRecord();
            record.setId(recordId);
            record.setTotalPrice(priceDTO.getTotalPrice());
            record.setUpdatedTime(LocalDateTime.now());

            return this.updateById(record);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================== 患者分析 - 图表数据 ==================

    @Override
    public List<Map<String, Object>> getDiseaseCauseTrendFromDiagnosis(Long hospitalId, int timeRange) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT diagnosis_name AS diseaseName, COUNT(*) AS count ");
        sql.append("FROM diagnosis d ");
        sql.append("WHERE d.diagnosis_name IS NOT NULL ");
        sql.append("  AND d.diagnosis_date IS NOT NULL ");
        sql.append("  AND d.diagnosis_date <= CURDATE() ");

        if (hospitalId != null) {
            sql.append("INNER JOIN patient p ON d.patient_id = p.id ");
            sql.append("INNER JOIN user u ON p.user_id = u.id ");
            sql.append("WHERE u.hospital_id = ? ");
            params.add(hospitalId);
        }

        sql.append("AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) ");
        params.add(timeRange);

        sql.append("GROUP BY diagnosis_name ORDER BY count DESC");

        try {
            System.out.println("【图表SQL】执行: " + sql.toString());
            System.out.println("【图表参数】: " + params);
            List<Map<String, Object>> result = jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("diseaseName", rs.getString("diseaseName"));
                row.put("count", rs.getInt("count"));
                return row;
            });
            System.out.println("【图表结果】: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getDiseaseTrendFromMedicalRecord(int timeRange, Long hospitalId) {
        return getDiseaseCauseTrendFromDiagnosis(hospitalId, timeRange);
    }

    @Override
    public List<Map<String, Object>> getDiseaseStatisticsFromHealthIndicator(int timeRange, Long hospitalId) {
        return null;
    }

    // 替换 getDiseaseTrendFromMedicalRecord 方法
    @Override
    public List<Map<String, Object>> getDiseaseTrendFromMedicalRecord(Long hospitalId, int timeRange) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // 使用 medical_record 表中的 diagnosis 字段（注意：可能是 diagnosis 或 diagnosis_name）
        sql.append("SELECT diagnosis AS diseaseName, COUNT(*) AS count ");
        sql.append("FROM medical_record ");
        sql.append("WHERE diagnosis IS NOT NULL ");
        sql.append("  AND visit_date IS NOT NULL ");
        sql.append("  AND visit_date <= CURDATE() ");
        sql.append("  AND visit_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) ");

        if (hospitalId != null) {
            sql.append("  AND patient_id IN (");
            sql.append("    SELECT id FROM patient WHERE user_id IN (");
            sql.append("      SELECT id FROM user WHERE hospital_id = ?)");
            sql.append("    )");
            params.add(hospitalId); // 注意顺序
        }

        sql.append("GROUP BY diagnosis ORDER BY count DESC");

        try {
            System.out.println("【执行SQL】: " + sql.toString());
            System.out.println("【参数】: " + params);

            return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("diseaseName", rs.getString("diseaseName"));
                row.put("count", rs.getInt("count"));
                return row;
            });
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈跟踪
            throw new RuntimeException("查询疾病趋势失败：" + e.getMessage());
        }
    }

    // 新增一个正确的方法
    private List<Map<String, Object>> getDiseaseTrendFromDiagnosis(Long hospitalId, int timeRange) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.diagnosis_name AS diseaseName, COUNT(*) AS count ");
        sql.append("FROM diagnosis d ");

        if (hospitalId != null) {
            sql.append("INNER JOIN patient p ON d.patient_id = p.id ");
            sql.append("INNER JOIN user u ON p.user_id = u.id ");
            sql.append("WHERE u.hospital_id = ? ");
            sql.append("  AND d.diagnosis_name IS NOT NULL ");
            sql.append("  AND d.diagnosis_date BETWEEN DATE_SUB(CURDATE(), INTERVAL ? MONTH) AND CURDATE() ");
        } else {
            sql.append("WHERE d.diagnosis_name IS NOT NULL ");
            sql.append("  AND d.diagnosis_date BETWEEN DATE_SUB(CURDATE(), INTERVAL ? MONTH) AND CURDATE() ");
        }

        sql.append("GROUP BY d.diagnosis_name ORDER BY count DESC");

        try {
            if (hospitalId != null) {
                return jdbcTemplate.query(sql.toString(), new Object[]{hospitalId, timeRange}, (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("diseaseName", rs.getString("diseaseName"));
                    row.put("count", rs.getInt("count"));
                    return row;
                });
            } else {
                return jdbcTemplate.query(sql.toString(), new Object[]{timeRange}, (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("diseaseName", rs.getString("diseaseName"));
                    row.put("count", rs.getInt("count"));
                    return row;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public int getDiagnosisCountByHospital(Long hospitalId, int timeRange) {
        String sql;
        List<Object> params = new ArrayList<>();
        params.add(timeRange);

        if (hospitalId != null) {
            sql = "SELECT COUNT(*) " +
                    "FROM diagnosis d " +
                    "INNER JOIN patient p ON d.patient_id = p.id " +
                    "INNER JOIN user u ON p.user_id = u.id " +
                    "WHERE u.hospital_id = ? " +
                    "  AND d.diagnosis_name IS NOT NULL " +
                    "  AND d.diagnosis_date IS NOT NULL " +
                    "  AND d.diagnosis_date <= CURDATE() " +
                    "  AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)";
            params.add(0, hospitalId);
        } else {
            sql = "SELECT COUNT(*) " +
                    "FROM diagnosis d " +
                    "WHERE d.diagnosis_name IS NOT NULL " +
                    "  AND d.diagnosis_date IS NOT NULL " +
                    "  AND d.diagnosis_date <= CURDATE() " +
                    "  AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)";
        }

        try {
            Integer count = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int countPatientsByHospitalFromDiagnosis(Long hospitalId, int timeRange) {
        String sql;
        List<Object> params = new ArrayList<>();
        params.add(timeRange);

        if (hospitalId != null) {
            sql = "SELECT COUNT(DISTINCT d.patient_id) " +
                    "FROM diagnosis d " +
                    "INNER JOIN patient p ON d.patient_id = p.id " +
                    "INNER JOIN user u ON p.user_id = u.id " +
                    "WHERE u.hospital_id = ? " +
                    "  AND d.diagnosis_name IS NOT NULL " +
                    "  AND d.diagnosis_date IS NOT NULL " +
                    "  AND d.diagnosis_date <= CURDATE() " +
                    "  AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)";
            params.add(0, hospitalId);
        } else {
            sql = "SELECT COUNT(DISTINCT d.patient_id) " +
                    "FROM diagnosis d " +
                    "WHERE d.diagnosis_name IS NOT NULL " +
                    "  AND d.diagnosis_date IS NOT NULL " +
                    "  AND d.diagnosis_date <= CURDATE() " +
                    "  AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)";
        }

        try {
            Integer count = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ================== 其他兼容方法 ==================

    @Override
    public int getDiagnosisCountFromMedicalRecord(Long hospitalId, int timeRange) {
        return getDiagnosisCountByHospital(hospitalId, timeRange);
    }

    @Override
    public int getTotalDiagnosisCountFromMedicalRecord(Long hospitalId) {
        return getDiagnosisCountByHospital(hospitalId, 12);
    }

    @Override
    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId) {
        return medicalRecordMapper.getMedicalRecordsByPatientId(patientId);
    }

    @Override
    public List<MedicalRecord> getByPatientId(Long patientId) {
        return listByPatientId(patientId);
    }
}