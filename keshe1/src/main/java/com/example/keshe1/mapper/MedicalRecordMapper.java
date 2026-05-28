package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.MedicalRecord;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    /**
     * 从medical_record表获取医院的疾病诊断趋势数据
     */
    List<Map<String, Object>> selectDiseaseTrendFromMedicalRecord(@Param("hospitalId") Long hospitalId,
                                                                  @Param("timeRange") int timeRange);

    /**
     * 获取医院的总诊断数（从medical_record表）- 不限制时间范围
     */
    int getTotalDiagnosisCountFromMedicalRecord(@Param("hospitalId") Long hospitalId);

    /**
     * 获取医院的总诊断数（从medical_record表）- 按时间范围
     */
    int getDiagnosisCountFromMedicalRecord(@Param("hospitalId") Long hospitalId,
                                           @Param("timeRange") int timeRange);

    /**
     * 从diagnosis表获取医院的疾病病因趋势数据
     */
    List<Map<String, Object>> selectDiseaseCauseTrendFromDiagnosis(@Param("hospitalId") Long hospitalId,
                                                                   @Param("timeRange") int timeRange);

    /**
     * 统计医院患者数量（从diagnosis表）
     */
    int countPatientsByHospitalFromDiagnosis(@Param("hospitalId") Long hospitalId,
                                             @Param("timeRange") int timeRange);

    /**
     * 获取医院的诊断总数（从diagnosis表）
     */
    int getDiagnosisCountByHospital(@Param("hospitalId") Long hospitalId,
                                    @Param("timeRange") int timeRange);

    //hsyxmk
    List<MedicalRecord> getMedicalRecordsByPatientId(@Param("patientId") Long patientId);
    // ... existing code ...

    List<Map<String, Object>> selectDiseaseTrendFromMedicalRecord(@Param("timeRange") int timeRange, @Param("hospitalId") Long hospitalId);
    // ... existing code ...

    /**
     * 医生端上半部分
     */
    // 统计本月接诊量 (根据 visit_date)
    @Select("SELECT COUNT(*) FROM medical_record WHERE doctor_id = #{doctorId} AND DATE_FORMAT(visit_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    Integer countMonthlyVisits(@Param("doctorId") Long doctorId);

    // 统计历史去重患者总数
    @Select("SELECT COUNT(DISTINCT patient_id) FROM medical_record WHERE doctor_id = #{doctorId}")
    Integer countTotalPatients(@Param("doctorId") Long doctorId);

    /**
     * 统计医生名下患者的年龄段分布
     * 逻辑：根据 birth_day 计算年龄，并按区间分组
     */
    @Select("SELECT " +
            "  CASE " +
            "    WHEN TIMESTAMPDIFF(YEAR, p.birth_date, CURDATE()) <= 18 THEN '0-18岁' " +
            "    WHEN TIMESTAMPDIFF(YEAR, p.birth_date, CURDATE()) BETWEEN 19 AND 35 THEN '19-35岁' " +
            "    WHEN TIMESTAMPDIFF(YEAR, p.birth_date, CURDATE()) BETWEEN 36 AND 50 THEN '36-50岁' " +
            "    WHEN TIMESTAMPDIFF(YEAR, p.birth_date, CURDATE()) BETWEEN 51 AND 65 THEN '51-65岁' " +
            "    ELSE '65岁以上' " +
            "  END AS age_group, " +
            "  COUNT(DISTINCT p.id) AS count " +
            "FROM medical_record mr " +
            "JOIN patient p ON mr.patient_id = p.id " +
            "WHERE mr.doctor_id = #{doctorId} " +
            "GROUP BY age_group")
    @MapKey("age_group")
    List<Map<String, Object>> getPatientAgeDistribution(@Param("doctorId") Long doctorId);
}