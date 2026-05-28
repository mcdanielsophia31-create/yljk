package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    
    /**
     * 根据医生ID和可选的姓名搜索患者
     */
    List<Patient> getByDoctorId(Long doctorId, String name);
    
    /**
     * 根据用户ID获取患者信息
     */
    Patient getByUserId(Long userId);
    
    /**
     * 根据姓名搜索患者
     */
    List<Patient> searchByName(String name);
    //hsy加

    List<Patient> getByHospitalIdAndName(Long hospitalId, String name);
    List<Patient> getByHospitalId(Long hospitalId);
    //
    Patient savePatient(Patient patient);

}