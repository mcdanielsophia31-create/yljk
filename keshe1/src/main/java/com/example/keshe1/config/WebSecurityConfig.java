package com.example.keshe1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // 静态资源放行
                        .antMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()
                        // 上传文件放行
                        .antMatchers("/uploads/**").permitAll()
                        // 登录注册接口放行
                        .antMatchers("/patient/login", "/patient/register").permitAll()
                        .antMatchers("/patient/forgot-password").permitAll()
                        .antMatchers("/doctor/login", "/doctor/bind-phone").permitAll()
                        .antMatchers("/doctor/forgot-password").permitAll()
                        .antMatchers("/admin/login").permitAll()
                        .antMatchers("/admin/forgot-password").permitAll()
                        // 临时放行药物管理接口以便调试
                        .antMatchers("/admin/medicines/**").permitAll()
                        // AI接口放行（精确匹配，优先级更高）
                        .antMatchers("/api/doubao/chat").permitAll()
                        .antMatchers("/api/doubao/**").permitAll()
                        // 角色权限控制
                        .antMatchers("/patient/**").hasAuthority("ROLE_PATIENT")
                        .antMatchers("/doctor/**").hasAuthority("ROLE_DOCTOR")
                        .antMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)  // 关闭CSRF便于前端调试
                .cors(AbstractHttpConfigurer::disable)   // 临时关闭跨域限制
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                );
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
