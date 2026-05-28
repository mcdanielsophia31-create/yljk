package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Department;
import com.example.keshe1.mapper.DepartmentMapper;
import com.example.keshe1.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Override
    public java.util.List<Department> getDepartmentsByHospitalId(Long hospitalId) {
        return departmentMapper.selectByHospitalId(hospitalId);
    }
}