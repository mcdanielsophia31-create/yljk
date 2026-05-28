package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.keshe1.entity.User;
import com.example.keshe1.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class PasswordInitializer {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 初始化时检查并加密所有未加密的密码
     * 注意：这是一个简单的判断逻辑，实际项目中应该有更好的方式来标识密码是否已加密
     */
    @PostConstruct
    public void initializePasswords() {
        // 查询所有用户
        List<User> users = userMapper.selectList(new QueryWrapper<>());
        
        for (User user : users) {
            String rawPassword = user.getPassword();
            // 简单判断密码是否已加密（BCRYPT加密后的密码以$2a$开头）
            if (rawPassword != null && !rawPassword.startsWith("$2a$")) {
                // 加密密码
                String encodedPassword = passwordEncoder.encode(rawPassword);
                user.setPassword(encodedPassword);
                userMapper.updateById(user);
                System.out.println("用户 " + user.getUsername() + " 的密码已加密");
            }
        }
    }
}