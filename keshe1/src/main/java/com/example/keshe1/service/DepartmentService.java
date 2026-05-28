package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {
    java.util.List<Department> getDepartmentsByHospitalId(Long hospitalId);
}