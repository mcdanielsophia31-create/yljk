package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.User;
import com.example.keshe1.mapper.UserMapper;
import com.example.keshe1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public User findByEmployeeId(String employeeId) {
        return userMapper.findByEmployeeId(employeeId);
    }

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }

        // 检查电话号码是否已存在
        if (findByPhone(user.getPhone()) != null) {
            return false;
        }

        // 医生账号只能由管理员创建
        if ("DOCTOR".equals(user.getUserType()) && !Boolean.TRUE.equals(user.getCreatedByAdmin())) {
            return false;
        }

        // 病人账号可以自行注册
        if ("PATIENT".equals(user.getUserType())) {
            user.setCreatedByAdmin(false);
        }

        // 验证医生工号是否符合医院编码规则
        if ("DOCTOR".equals(user.getUserType()) && user.getEmployeeId() != null) {
            String employeeId = user.getEmployeeId();
            Long hospitalId = user.getHospitalId();

            // 验证工号长度为9位
            if (employeeId.length() != 9) {
                return false;
            }

            // 验证工号为纯数字
            if (!employeeId.matches("\\d+")) {
                return false;
            }

            // 验证前3位与医院编码一致（这里简化处理，实际应该查询医院表获取编码）
            String hospitalCode = String.format("%03d", hospitalId);
            if (!employeeId.startsWith(hospitalCode)) {
                return false;
            }
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认状态为未激活
        user.setStatus("INACTIVE");

        // 保存用户
        return save(user);
    }



    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            updateById(user);
            return user;
        }
        return null;
    }

    @Override
    public User loginByUsername(String username, String password) {
        User user = findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            updateById(user);
            return user;
        }
        return null;
    }

    @Override
    public User loginByPhone(String phone, String password) {
        User user = findByPhone(phone);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            updateById(user);
            return user;
        }
        return null;
    }

    @Override
    public boolean isPhoneUnique(String phone) {
        return userMapper.selectCount(new QueryWrapper<User>().eq("phone", phone)) == 0;
    }

    // ... 现有方法

    @Override
    public boolean updateAvatar(Long userId, String avatarPath) {
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(avatarPath);
                return userMapper.updateById(user) > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean verifyPassword(Long userId, String currentPassword) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return false;
            }

            // 使用BCrypt验证密码
            return passwordEncoder.matches(currentPassword, user.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return false;
            }

            // 使用BCrypt加密新密码
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);

            int rows = userMapper.updateById(user);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<User> getDoctorsByDepartmentAndHospital(Long departmentId, Long hospitalId) {
        return userMapper.getDoctorsByDepartmentAndHospital(departmentId, hospitalId);
    }
}