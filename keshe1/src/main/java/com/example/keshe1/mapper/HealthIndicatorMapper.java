package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.keshe1.dto.MonthlyBloodPressureDTO;
import com.example.keshe1.entity.HealthIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HealthIndicatorMapper extends BaseMapper<HealthIndicator> {

    /**
     * 调用存储过程获取病人月度平均血压
     * @param patientId 病人ID
     * @return 月度血压数据列表
     */
    @Select("CALL GetPatientMonthlyAvgBloodPressure(#{patientId})")
    List<MonthlyBloodPressureDTO> getMonthlyBloodPressureByPatientId(@Param("patientId") Long patientId);
}