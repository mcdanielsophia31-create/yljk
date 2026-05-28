package com.example.keshe1.service;

import com.example.keshe1.entity.Doctor;
import com.example.keshe1.entity.Patient;
import com.example.keshe1.entity.User;

/**
 * 用户信息服务，用于处理user、doctor、patient表之间的关联查询
 * 确保用户信息的一致性
 */
public interface UserInfoService {
    
    /**
     * 根据用户ID获取完整的医生信息（包括用户基本信息）
     * @param userId 用户ID
     * @return 包含用户信息的医生对象
     */
    Doctor getDoctorWithUserInfo(Long userId);
    
    /**
     * 根据用户ID获取完整的患者信息（包括用户基本信息）
     * @param userId 用户ID
     * @return 包含用户信息的患者对象
     */
    Patient getPatientWithUserInfo(Long userId);
    
    /**
     * 更新用户基本信息，并同步更新关联的医生或患者信息
     * @param user 用户对象
     * @return 更新是否成功
     */
    boolean updateUserInfo(User user);
}