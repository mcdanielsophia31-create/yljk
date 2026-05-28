package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.keshe1.entity.Doctor;
import com.example.keshe1.entity.Patient;
import com.example.keshe1.entity.User;
import com.example.keshe1.mapper.DoctorMapper;
import com.example.keshe1.mapper.PatientMapper;
import com.example.keshe1.mapper.UserMapper;
import com.example.keshe1.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息服务实现类
 * 处理user、doctor、patient表之间的关联查询，确保数据一致性
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private PatientMapper patientMapper;

    @Override
    public Doctor getDoctorWithUserInfo(Long userId) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return null;
        }
        
        // 获取医生信息
        QueryWrapper<Doctor> doctorQuery = new QueryWrapper<>();
        doctorQuery.eq("user_id", userId);
        Doctor doctor = doctorMapper.selectOne(doctorQuery);
        
        if (doctor != null) {
            // 将用户基本信息设置到医生对象中（如果需要在前端显示）
            // 注意：由于我们已经移除了doctor表中的冗余字段，这里主要是为了业务逻辑需要
        }
        
        return doctor;
    }

    @Override
    public Patient getPatientWithUserInfo(Long userId) {
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return null;
        }
        
        // 获取患者信息
        QueryWrapper<Patient> patientQuery = new QueryWrapper<>();
        patientQuery.eq("user_id", userId);
        Patient patient = patientMapper.selectOne(patientQuery);
        
        if (patient != null) {
            // 将用户基本信息设置到患者对象中（如果需要在前端显示）
            // 注意：由于我们已经移除了patient表中的冗余字段，这里主要是为了业务逻辑需要
        }
        
        return patient;
    }

    @Override
    @Transactional
    public boolean updateUserInfo(User user) {
        // 更新用户表
        int result = userMapper.updateById(user);
        if (result <= 0) {
            return false;
        }
        
        // 根据用户类型，可能需要更新关联表的某些信息
        if ("DOCTOR".equals(user.getUserType()) && user.getRelatedId() != null) {
            // 如果是医生用户，可以更新医生表的某些信息（如果有的话）
            // 在当前优化结构中，医生表不再包含冗余的姓名、电话、邮箱字段
        } else if ("PATIENT".equals(user.getUserType()) && user.getRelatedId() != null) {
            // 如果是患者用户，可以更新患者表的某些信息（如果有的话）
            // 在当前优化结构中，患者表不再包含冗余的姓名、电话、邮箱字段
        }
        
        return true;
    }
}