package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Diagnosis;

import java.util.List;
import java.util.Map;

public interface DiagnosisService extends IService<Diagnosis> {
    
    /**
     * 根据患者ID获取诊断记录
     */
    List<Diagnosis> getByPatientId(Long patientId);
    
    /**
     * 根据病历ID获取诊断记录
     */
    List<Diagnosis> getByRecordId(Long recordId);
    
    /**
     * 根据患者ID统计诊断记录数量
     */
    int countByPatientId(Long patientId);

    //hsy
    // 在DiagnosisService接口中添加方法
    // 新增方法
    List<Map<String, Object>> getDiseaseTrend(Long hospitalId);
    // 更新DiagnosisService接口中的方法
    /**
     * 根据患者ID获取诊断记录
     */
    List<Diagnosis> getDiagnosesByPatientId(Long patientId);

    /**
     * 根据病历ID获取诊断记录
     */
    List<Diagnosis> getDiagnosesByMedicalRecordId(Long medicalRecordId);

    /**
     * 获取最近的诊断记录
     */
    List<Diagnosis> getRecentDiagnoses(int limit);

    /**
     * 获取疾病趋势数据
     */
    List<Map<String, Object>> getDiseaseTrend(Long hospitalId, int timeRange);
    /**
     * 根据医院ID获取诊断总数
     */
    int getDiagnosisCountByHospitalId(Long hospitalId);
    /**
     * 根据医院ID获取诊断记录
     */
    List<Diagnosis> getDiagnosesByHospitalId(Long hospitalId);
}