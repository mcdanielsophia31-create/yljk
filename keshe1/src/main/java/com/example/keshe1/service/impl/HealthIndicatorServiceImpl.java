package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.keshe1.dto.MonthlyBloodPressureDTO;
import com.example.keshe1.entity.HealthIndicator;
import com.example.keshe1.mapper.HealthIndicatorMapper;
import com.example.keshe1.service.HealthIndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthIndicatorServiceImpl extends ServiceImpl<HealthIndicatorMapper, HealthIndicator>
        implements HealthIndicatorService {

    @Autowired
    private HealthIndicatorMapper healthIndicatorMapper;

    @Override
    public List<MonthlyBloodPressureDTO> getMonthlyBloodPressureByPatientId(Long patientId) {
        return healthIndicatorMapper.getMonthlyBloodPressureByPatientId(patientId);
    }
}