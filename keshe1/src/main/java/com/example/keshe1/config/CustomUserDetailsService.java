package com.example.keshe1.config;

import com.example.keshe1.entity.User;
import com.example.keshe1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        String foundByUsername = null; // 记录找到用户的字段
        
        // 首先尝试通过用户名查找用户
        user = userService.findByUsername(username);
        if (user != null) {
            foundByUsername = user.getUsername();
        }
        
        if (user == null) {
            // 如果用户名没找到，尝试通过电话号码查找
            user = userService.findByPhone(username);
            if (user != null) {
                foundByUsername = user.getPhone();
            }
        }
        
        if (user == null) {
            // 如果电话号码没找到，尝试通过工号查找
            user = userService.findByEmployeeId(username);
            if (user != null) {
                foundByUsername = user.getEmployeeId();
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 将用户类型转换为角色
        List<GrantedAuthority> authorities = new ArrayList<>();
        String role = "ROLE_" + user.getUserType(); // 例如: ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN
        authorities.add(new SimpleGrantedAuthority(role));

        // 返回Spring Security的UserDetails对象
        // 使用找到的用户标识符作为认证用户名，确保认证成功
        return new org.springframework.security.core.userdetails.User(
                foundByUsername,         // 使用找到的用户名进行认证
                user.getPassword(),      // 加密后的密码
                authorities             // 用户角色
        );
    }
    
    /**
     * 根据用户名和医院ID查找用户
     * @param username 用户名、电话或工号
     * @param hospitalId 医院ID
     * @return 用户信息
     */
    public User loadUserByUsernameAndHospital(String username, Long hospitalId) throws UsernameNotFoundException {
        User user = null;
        String foundByUsername = null; // 记录找到用户的字段
        
        // 首先尝试通过用户名查找用户
        user = userService.findByUsername(username);
        if (user != null) {
            foundByUsername = user.getUsername();
        }
        
        if (user == null) {
            // 如果用户名没找到，尝试通过电话号码查找
            user = userService.findByPhone(username);
            if (user != null) {
                foundByUsername = user.getPhone();
            }
        }
        
        if (user == null) {
            // 如果电话号码没找到，尝试通过工号查找
            user = userService.findByEmployeeId(username);
            if (user != null) {
                foundByUsername = user.getEmployeeId();
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 验证用户是否属于指定医院（仅对医生和管理员进行医院验证）
        if (("DOCTOR".equals(user.getUserType()) || "ADMIN".equals(user.getUserType())) 
            && hospitalId != null && !hospitalId.equals(user.getHospitalId())) {
            throw new UsernameNotFoundException("用户不属于指定医院: " + username);
        }

        return user;
    }
}