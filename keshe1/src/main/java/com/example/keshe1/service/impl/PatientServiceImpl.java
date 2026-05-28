package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.MedicalRecord;
import com.example.keshe1.entity.Patient;
import com.example.keshe1.mapper.PatientMapper;
import com.example.keshe1.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public List<Patient> getByDoctorId(Long doctorId, String name) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        // 这里假设有一个关联字段，实际应该根据业务需求调整
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        return patientMapper.selectList(queryWrapper);
    }


    // ... existing code ...
//    @Override
//    public Patient getByUserId(Long userId) {
//        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userId);
//        return patientMapper.selectOne(queryWrapper);
//    }

    //xmkhsy

    @Override
    public List<Patient> searchByName(String name) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        return patientMapper.selectList(queryWrapper);
    }

    //hsy加
// 在PatientServiceImpl.java中
    // 在PatientServiceImpl.java中确保正确获取数据
    @Override
    public List<Patient> getByHospitalId(Long hospitalId) {
        // 获取符合条件的患者ID
        List<Long> patientIds = patientMapper.selectIdsByHospitalId(hospitalId);
        if (patientIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用ID列表获取完整患者信息
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", patientIds);
        List<Patient> patients = patientMapper.selectList(queryWrapper);

        return patients;
    }

    @Override
    public List<Patient> getByHospitalIdAndName(Long hospitalId, String name) {
        if (name == null || name.trim().isEmpty()) {
            return getByHospitalId(hospitalId);
        }

        // 获取符合条件的患者ID
        List<Long> patientIds = patientMapper.selectIdsByHospitalIdAndName(hospitalId, name);
        if (patientIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用ID列表获取完整患者信息
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", patientIds);
        List<Patient> patients = patientMapper.selectList(queryWrapper);

        return patients;
    }
    //
// ... existing code ...

    @Override
    public Patient getByUserId(Long userId) {
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Patient patient = this.getOne(queryWrapper);
        System.out.println("根据用户ID " + userId + " 查询到患者: " + patient);
        return patient;
    }

// ... existing code ...
@Override
public Patient savePatient(Patient patient) {
    // 使用MyBatis Plus的save方法
    boolean success = save(patient);
    if (success) {
        return patient; // MyBatis Plus会回填ID
    }
    throw new RuntimeException("保存患者信息失败");
}
}