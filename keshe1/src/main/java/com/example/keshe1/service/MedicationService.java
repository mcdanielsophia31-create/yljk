package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Medication;

import java.util.List;

public interface MedicationService extends IService<Medication> {
    
    /**
     * 根据患者ID获取用药记录
     */
    List<Medication> getByPatientId(Long patientId);
    
    /**
     * 根据病历ID获取用药记录
     */
    List<Medication> getByRecordId(Long recordId);
    
    /**
     * 根据患者ID统计用药记录数量
     */
    int countByPatientId(Long patientId);
}