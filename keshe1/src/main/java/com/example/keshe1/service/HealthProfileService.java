package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.HealthProfile;

import java.util.List;

public interface HealthProfileService extends IService<HealthProfile> {

    /**
     * 根据患者ID获取健康档案
     */
    List<HealthProfile> getByPatientId(Long patientId);


}