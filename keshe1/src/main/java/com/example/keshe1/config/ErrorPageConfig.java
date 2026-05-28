package com.example.keshe1.config;

import lombok.var;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class ErrorPageConfig {

    @Controller
    public static class CustomErrorController implements ErrorController {

        @RequestMapping("/error")
        public String handleError(HttpServletRequest request, Model model) {
            // 获取错误状态码
            Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            
            // 检查用户是否已认证
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
            
            // 如果用户未认证且尝试访问受保护的页面，重定向到登录页
            if (!isAuthenticated) {
                return "redirect:/login";
            }

            // 获取用户角色，传递给模板
            String userRole = "admin"; // 默认为admin
            if (auth != null && auth.getAuthorities() != null) {
                for (var authority : auth.getAuthorities()) {
                    String role = authority.getAuthority();
                    if ("ROLE_DOCTOR".equals(role)) {
                        userRole = "doctor";
                        break;
                    } else if ("ROLE_PATIENT".equals(role)) {
                        userRole = "patient";
                        break;
                    } else if ("ROLE_ADMIN".equals(role)) {
                        userRole = "admin";
                        break;
                    }
                }
            }
            
            model.addAttribute("userRole", userRole);
            model.addAttribute("user", auth != null ? auth.getPrincipal() : null);
            
            // 如果用户已认证，显示错误页面
            // 403 - 访问被拒绝（用户已登录但没有权限访问该页面）
            // 404 - 页面未找到（用户已登录但访问不存在的页面）
            // 500 - 服务器错误
            return "error";
        }
    }
}