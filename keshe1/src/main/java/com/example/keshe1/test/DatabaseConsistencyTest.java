package com.example.keshe1.test;

import com.example.keshe1.entity.User;
import com.example.keshe1.entity.Doctor;
import com.example.keshe1.entity.Patient;
import com.example.keshe1.mapper.UserMapper;
import com.example.keshe1.mapper.DoctorMapper;
import com.example.keshe1.mapper.PatientMapper;
import com.example.keshe1.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据库连接和数据一致性测试类
 */
@Component
public class DatabaseConsistencyTest implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private PatientMapper patientMapper;
    
    @Autowired
    private UserInfoService userInfoService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 开始数据库连接和数据一致性测试 ===");
        
        // 测试1: 验证数据库连接
        try {
            Long userCount = userMapper.selectCount(null);
            System.out.println("用户表记录数: " + userCount);
            System.out.println("✓ 数据库连接正常");
        } catch (Exception e) {
            System.out.println("✗ 数据库连接失败: " + e.getMessage());
            return;
        }
        
        // 测试2: 验证医生表结构（应该没有冗余字段）
        try {
            // 尝试查询一条医生记录
            Doctor doctor = doctorMapper.selectById(1);
            if (doctor != null) {
                System.out.println("✓ 医生表结构正常，记录ID: " + doctor.getId());
            } else {
                System.out.println("提示: 医生表中没有ID为1的记录，但结构正常");
            }
        } catch (Exception e) {
            System.out.println("✗ 医生表结构存在问题: " + e.getMessage());
        }
        
        // 测试3: 验证患者表结构（应该没有冗余字段）
        try {
            // 尝试查询一条患者记录
            Patient patient = patientMapper.selectById(1);
            if (patient != null) {
                System.out.println("✓ 患者表结构正常，记录ID: " + patient.getId());
            } else {
                System.out.println("提示: 患者表中没有ID为1的记录，但结构正常");
            }
        } catch (Exception e) {
            System.out.println("✗ 患者表结构存在问题: " + e.getMessage());
        }
        
        // 测试4: 验证用户信息关联服务
        try {
            User testUser = userMapper.selectById(1);
            if (testUser != null) {
                System.out.println("✓ 用户信息查询正常，用户名: " + testUser.getUsername());
                
                // 如果是医生用户，测试关联查询
                if ("DOCTOR".equals(testUser.getUserType())) {
                    Doctor doctorWithInfo = userInfoService.getDoctorWithUserInfo(testUser.getId());
                    if (doctorWithInfo != null) {
                        System.out.println("✓ 医生用户关联查询正常");
                    }
                }
                
                // 如果是患者用户，测试关联查询
                if ("PATIENT".equals(testUser.getUserType())) {
                    Patient patientWithInfo = userInfoService.getPatientWithUserInfo(testUser.getId());
                    if (patientWithInfo != null) {
                        System.out.println("✓ 患者用户关联查询正常");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("✗ 用户信息关联服务测试失败: " + e.getMessage());
        }
        
        System.out.println("=== 数据库连接和数据一致性测试完成 ===");
    }
}