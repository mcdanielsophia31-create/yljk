package com.example.keshe1.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.keshe1.entity.User;
import com.example.keshe1.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统初始化配置
 * 在应用启动时检查并加密数据库中的明文密码
 */
@Component
public class SystemInitConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 检查并加密明文密码
        encryptPlainPasswords();
    }

    /**
     * 加密数据库中的明文密码
     * 判断密码是否为明文的简单方法：长度较短且不含特殊字符
     */
    private void encryptPlainPasswords() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(queryWrapper);

        for (User user : users) {
            String password = user.getPassword();
            // 简单判断是否为明文密码（BCrypt加密后的密码通常较长且包含特殊字符）
            if (password != null && password.length() < 50 && !password.contains("$2a$")) {
                // 对明文密码进行BCrypt加密
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
                userMapper.updateById(user);
            }
        }
    }
}