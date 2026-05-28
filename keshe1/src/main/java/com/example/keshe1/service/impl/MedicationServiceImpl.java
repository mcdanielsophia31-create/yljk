package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Medication;
import com.example.keshe1.mapper.MedicationMapper;
import com.example.keshe1.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationServiceImpl extends ServiceImpl<MedicationMapper, Medication> implements MedicationService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Medication> getByPatientId(Long patientId) {
        QueryWrapper<Medication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.orderByDesc("prescription_date");
        return this.list(queryWrapper);
    }

    @Override
    public List<Medication> getByRecordId(Long recordId) {
        QueryWrapper<Medication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_id", recordId);
        return this.list(queryWrapper);
    }

    @Override
    public int countByPatientId(Long patientId) {
        QueryWrapper<Medication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        return (int) this.count(queryWrapper);
    }

}