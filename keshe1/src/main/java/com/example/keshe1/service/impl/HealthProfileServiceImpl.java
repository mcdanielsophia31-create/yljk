package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.HealthProfile;
import com.example.keshe1.mapper.HealthProfileMapper;
import com.example.keshe1.service.HealthProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthProfileServiceImpl extends ServiceImpl<HealthProfileMapper, HealthProfile> implements HealthProfileService {

    @Autowired
    private HealthProfileMapper healthProfileMapper;

    // 核心修改：返回值改为 List<HealthProfile>
    @Override
    public List<HealthProfile> getByPatientId(Long patientId) {
        return healthProfileMapper.getByPatientId(patientId);
    }




}