package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Diagnosis;
import com.example.keshe1.mapper.DiagnosisMapper;
import com.example.keshe1.mapper.MedicalRecordMapper;
import com.example.keshe1.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DiagnosisServiceImpl extends ServiceImpl<DiagnosisMapper, Diagnosis> implements DiagnosisService {


    //hsy
    @Autowired
    private DiagnosisMapper diagnosisMapper;


    @Override
    public List<Map<String, Object>> getDiseaseTrend(Long hospitalId) {
        // 调用默认12个月的查询
        return diagnosisMapper.selectDiseaseTrendDefault(hospitalId);
    }


    @Override
    public List<Diagnosis> getDiagnosesByPatientId(Long patientId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.orderByDesc("diagnosis_date");
        return diagnosisMapper.selectList(queryWrapper);
    }

    @Override
    public List<Diagnosis> getDiagnosesByMedicalRecordId(Long medicalRecordId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("medical_record_id", medicalRecordId);
        queryWrapper.orderByDesc("diagnosis_date");
        return diagnosisMapper.selectList(queryWrapper);
    }

    @Override
    public List<Diagnosis> getRecentDiagnoses(int limit) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("diagnosis_date");
        queryWrapper.last("LIMIT " + limit);
        return diagnosisMapper.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getDiseaseTrend(Long hospitalId, int timeRange) {
        if (timeRange == 12) { // 如果是默认的12个月，使用默认查询
            return diagnosisMapper.selectDiseaseTrendDefault(hospitalId);
        } else {
            return diagnosisMapper.selectDiseaseTrend(hospitalId, timeRange);
        }
    }

    @Override
    public int getDiagnosisCountByHospitalId(Long hospitalId) {
        return diagnosisMapper.getDiagnosisCountByHospitalId(hospitalId);
    }

    @Override
    public List<Diagnosis> getDiagnosesByHospitalId(Long hospitalId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.inSql("patient_id",
                "SELECT id FROM patient WHERE user_id IN (SELECT id FROM user WHERE hospital_id = " + hospitalId + ")");
        queryWrapper.orderByDesc("diagnosis_date");
        return diagnosisMapper.selectList(queryWrapper);
    }

    // 其他现有方法保持不变
    @Override
    public List<Diagnosis> getByPatientId(Long patientId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.orderByDesc("diagnosis_date");
        return this.list(queryWrapper);
    }

    @Override
    public List<Diagnosis> getByRecordId(Long recordId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_id", recordId);
        return this.list(queryWrapper);
    }

    @Override
    public int countByPatientId(Long patientId) {
        QueryWrapper<Diagnosis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        return (int) this.count(queryWrapper);
    }


}