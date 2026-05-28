package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.dto.HospitalMedicalRecordDTO;
import com.example.keshe1.dto.MedicalRecordPriceDTO;
import com.example.keshe1.entity.MedicalRecord;
import com.example.keshe1.mapper.MedicalRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public interface MedicalRecordService extends IService<MedicalRecord> {
    
    /**
     * 根据患者ID获取病历记录
     */
    List<MedicalRecord> listByPatientId(Long patientId);
    
    /**
     * 根据患者ID获取病历记录
     */
    List<MedicalRecord> getByPatientId(Long patientId);
    
    /**
     * 根据医生ID获取病历记录
     */
    List<MedicalRecord> getByDoctorId(Long doctorId);
    
    /**
     * 根据患者ID统计病历记录数量
     */
    int countByPatientId(Long patientId);

    /**
     * 处方
     */
    boolean updateTreatmentPlan(Long recordId, String treatmentPlan);

    /**
     * 根据医生ID获取所属医院的所有病历（通过存储过程）
     */
    List<HospitalMedicalRecordDTO> getHospitalMedicalRecordsByDoctorId(Long doctorId);

    /**
     * 输入患者姓名模糊查询
     */
    List<HospitalMedicalRecordDTO> searchMedicalRecords(Long doctorId, String keyword);

    /**
     * 创建新病历
     */
    MedicalRecord createMedicalRecord(Long doctorId, Long patientId);



    /**
     * 获取医院的疾病诊断趋势数据
     */
    List<Map<String, Object>> getDiseaseTrendFromMedicalRecord(Long hospitalId, int timeRange);

    //hsy
    /**
     * 获取医院的总诊断数（从medical_record表）
     */
    int getDiagnosisCountFromMedicalRecord(Long hospitalId, int timeRange);
    int getTotalDiagnosisCountFromMedicalRecord(Long hospitalId);

    /**
     * 获取医院的总诊断数（从medical_record表）
     */
    /**
     * 从diagnosis表获取医院的疾病病因趋势数据
     */
    List<Map<String, Object>> getDiseaseCauseTrendFromDiagnosis(Long hospitalId, int timeRange);

    /**
     * 统计医院患者数量（从diagnosis表）
     */
    int countPatientsByHospitalFromDiagnosis(Long hospitalId, int timeRange);

    /**
     * 获取医院的诊断总数（从diagnosis表）
     */
    int getDiagnosisCountByHospital(Long hospitalId, int timeRange);

    // ... existing code ...

    /**
     * 从medical_record表获取疾病趋势数据
     */
    List<Map<String, Object>> getDiseaseTrendFromMedicalRecord(int timeRange, Long hospitalId);
    List<Map<String, Object>> getDiseaseStatisticsFromHealthIndicator(int timeRange, Long hospitalId);

    /**
     * 计算病历总金额（挂号费 + 药费 + 检查费）
     */
    MedicalRecordPriceDTO calculateTotalPrice(Long recordId);

    /**
     * 更新病历总金额到数据库
     */
    boolean updateTotalPrice(Long recordId);

    // ... existing code ...
    //hsyxmk
    List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId);

}