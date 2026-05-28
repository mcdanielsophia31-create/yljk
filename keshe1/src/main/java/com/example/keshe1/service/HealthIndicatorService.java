package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.dto.MonthlyBloodPressureDTO;
import com.example.keshe1.entity.HealthIndicator;


import java.util.List;

public interface HealthIndicatorService extends IService<HealthIndicator> {

    /**
     * 获取病人月度平均血压数据
     * @param patientId 病人ID
     * @return 月度血压数据列表
     */
    List<MonthlyBloodPressureDTO> getMonthlyBloodPressureByPatientId(Long patientId);
}