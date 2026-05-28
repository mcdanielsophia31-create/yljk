// 在DiagnosisMapper.java中添加方法
package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.Diagnosis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DiagnosisMapper extends BaseMapper<Diagnosis> {

    // 新增方法 - 按默认12个月查询，按医院过滤
    @Select("SELECT d.diagnosis_name as diseaseName, " +
            "DATE_FORMAT(d.diagnosis_date, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM diagnosis d " +
            "INNER JOIN medical_record mr ON d.medical_record_id = mr.id " +
            "INNER JOIN patient p ON mr.patient_id = p.id " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId} " +
            "AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
            "GROUP BY d.diagnosis_name, DATE_FORMAT(d.diagnosis_date, '%Y-%m') " +
            "ORDER BY d.diagnosis_date")
    List<Map<String, Object>> selectDiseaseTrendDefault(@Param("hospitalId") Long hospitalId);

    // 新增方法 - 按指定时间范围查询，按医院过滤
    @Select("SELECT d.diagnosis_name as diseaseName, " +
            "DATE_FORMAT(d.diagnosis_date, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM diagnosis d " +
            "INNER JOIN medical_record mr ON d.medical_record_id = mr.id " +
            "INNER JOIN patient p ON mr.patient_id = p.id " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId} " +
            "AND d.diagnosis_date >= DATE_SUB(CURDATE(), INTERVAL #{timeRange} MONTH) " +
            "GROUP BY d.diagnosis_name, DATE_FORMAT(d.diagnosis_date, '%Y-%m') " +
            "ORDER BY d.diagnosis_date")
    List<Map<String, Object>> selectDiseaseTrend(@Param("hospitalId") Long hospitalId,
                                                 @Param("timeRange") int timeRange);
    // 添加一个方法来检查当前医院的诊断总数
    @Select("SELECT COUNT(*) " +
            "FROM diagnosis d " +
            "INNER JOIN medical_record mr ON d.medical_record_id = mr.id " +
            "INNER JOIN patient p ON mr.patient_id = p.id " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId}")
    int getDiagnosisCountByHospitalId(@Param("hospitalId") Long hospitalId);
    // ... existing code ...

    /**
     * 从medical_record表获取医院的疾病诊断趋势数据
     */
    @Select("SELECT mr.diagnosis as diseaseName, " +
            "DATE_FORMAT(mr.visit_date, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM medical_record mr " +
            "INNER JOIN patient p ON mr.patient_id = p.id " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId} " +
            "AND mr.diagnosis IS NOT NULL " +
            "AND mr.diagnosis != '' " +
            "AND mr.visit_date >= DATE_SUB(CURDATE(), INTERVAL #{timeRange} MONTH) " +
            "GROUP BY mr.diagnosis, DATE_FORMAT(mr.visit_date, '%Y-%m') " +
            "ORDER BY mr.visit_date")
    List<Map<String, Object>> selectDiseaseTrendFromMedicalRecord(@Param("hospitalId") Long hospitalId,
                                                                  @Param("timeRange") int timeRange);

// ... existing code ...
}