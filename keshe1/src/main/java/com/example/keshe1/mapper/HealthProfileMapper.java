package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.HealthProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HealthProfileMapper extends BaseMapper<HealthProfile> {

    /**
     * 根据患者ID获取健康档案
     */
    @Select("SELECT * FROM health_profile WHERE patient_id = #{patientId}")
    List<HealthProfile> getByPatientId(@Param("patientId") Long patientId);



}