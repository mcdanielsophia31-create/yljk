package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据电话号码查找用户
     */
    User findByPhone(String phone);

    /**
     * 根据工号查找用户
     */
    User findByEmployeeId(String employeeId);

    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 通过用户名登录
     */
    User loginByUsername(String username, String password);

    /**
     * 通过电话号码登录
     */
    User loginByPhone(String phone, String password);

    /**
     * 验证电话号码是否唯一
     */
    boolean isPhoneUnique(String phone);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarPath 头像路径
     * @return 是否更新成功
     */
    boolean updateAvatar(Long userId, String avatarPath);

    /**
     * 根据用户ID获取用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getById(Long id);
    boolean verifyPassword(Long userId, String currentPassword);
    boolean updatePassword(Long userId, String newPassword);
    
    /**
     * 根据科室ID和医院ID获取医生用户列表
     */
    List<User> getDoctorsByDepartmentAndHospital(Long departmentId, Long hospitalId);
}