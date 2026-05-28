package com.example.keshe1.config;

import com.example.keshe1.entity.User;
import com.example.keshe1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // 从认证信息中获取用户角色
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // 获取用户名
        String username = authentication.getName();
        
        // 获取用户信息用于医院验证
        User user = userService.findByUsername(username);
        if (user == null) {
            user = userService.findByPhone(username);
        }
        if (user == null) {
            user = userService.findByEmployeeId(username);
        }
        
        // 检查是否有医院ID参数，这通常意味着是机构人员登录
        String hospitalIdParam = request.getParameter("hospitalId");
        boolean isInstitutionLogin = hospitalIdParam != null && !hospitalIdParam.trim().isEmpty();
        String roleParam = request.getParameter("role"); // 可取值：ADMIN 或 DOCTOR

        // 获取用户类型
        String userType = user != null ? user.getUserType() : null;
        
        // 验证用户类型与登录意图是否匹配
        if (isInstitutionLogin && "PATIENT".equals(userType)) {
            // 患者用户尝试通过机构登录界面登录
            response.sendRedirect(request.getContextPath() + "/login?error=wrong_user_type");
            return;
        } else if (!isInstitutionLogin && ("DOCTOR".equals(userType) || "ADMIN".equals(userType))) {
            // 医生或管理员用户尝试通过普通登录界面登录
            response.sendRedirect(request.getContextPath() + "/login?error=wrong_user_type");
            return;
        }
        
        // 机构登录下进一步根据角色限制：医生不能用管理员入口，管理员不能用医生入口
        if (isInstitutionLogin && roleParam != null && !roleParam.trim().isEmpty()) {
            if ("ADMIN".equals(roleParam) && !"ADMIN".equals(userType)) {
                response.sendRedirect(request.getContextPath() + "/login?error=wrong_user_type");
                return;
            }
            if ("DOCTOR".equals(roleParam) && !"DOCTOR".equals(userType)) {
                response.sendRedirect(request.getContextPath() + "/login?error=wrong_user_type");
                return;
            }
        }
        
        // 验证医院ID（仅对医生和管理员进行医院验证）
        if (user != null && ("DOCTOR".equals(user.getUserType()) || "ADMIN".equals(user.getUserType()))
                && hospitalIdParam != null && !hospitalIdParam.trim().isEmpty()) {
            try {
                Long hospitalId = Long.parseLong(hospitalIdParam);
                if (!hospitalId.equals(user.getHospitalId())) {
                    // 医院不匹配，重定向到登录页面并显示错误
                    response.sendRedirect(request.getContextPath() + "/login?error=hospital_mismatch");
                    return;
                }
            } catch (NumberFormatException e) {
                // 无效的医院ID，重定向到登录页面并显示错误
                response.sendRedirect(request.getContextPath() + "/login?error=invalid_hospital_id");
                return;
            }
        }
        
        // 根据角色直接跳转，避免再次查询数据库
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            
            if ("ROLE_PATIENT".equals(role)) {
                response.sendRedirect("/patient/dashboard");
                return;
            } else if ("ROLE_DOCTOR".equals(role)) {
                response.sendRedirect("/doctor/dashboard");
                return;
            } else if ("ROLE_ADMIN".equals(role)) {
                response.sendRedirect("/admin/dashboard");
                return;
            }
        }
        
        // 如果没有匹配的角色，根据用户偏好不显示错误，默认跳转到首页
        response.sendRedirect("/");
    }
}
