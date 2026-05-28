package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.example.keshe1.entity.Examination;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface ExaminationMapper extends BaseMapper<Examination> {

    /**
     * 关联 examination_item, patient, doctor, user 表
     * ${ew.customSqlSegment} 会自动拼接 Service 层传进来的 WHERE 条件
     */
    @Select("SELECT e.*, " +
            "ei.examination_item AS examinationItemName, " +
            "ei.examination_type AS examinationTypeName, " +
            "ei.price AS price, " +
            "pu.real_name AS patientName, " + // 患者真实姓名
            "du.real_name AS doctorName " +   // 医生真实姓名
            "FROM examination e " +
            "LEFT JOIN examination_item ei ON e.examination_item_id = ei.id " +
            "LEFT JOIN patient p ON e.patient_id = p.id " +
            "LEFT JOIN user pu ON p.user_id = pu.id " +
            "LEFT JOIN doctor d ON e.doctor_id = d.id " +
            "LEFT JOIN user du ON d.user_id = du.id " +
            "${ew.customSqlSegment}")
    List<Examination> selectExaminationDetails(@Param(Constants.WRAPPER) Wrapper<Examination> wrapper);
}