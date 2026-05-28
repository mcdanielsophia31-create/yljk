package com.example.keshe1.controller;

import com.example.keshe1.dto.MonthlyBloodPressureDTO;
import com.example.keshe1.entity.*;
import com.example.keshe1.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.keshe1.entity.Medication;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private HealthProfileService healthProfileService;

    @Autowired
    private HealthIndicatorService healthIndicatorService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private ExaminationItemService examinationItemService;

    /**
     * 获取或创建患者信息
     */
    private Patient getOrCreatePatient(User user, HttpSession session) {
        // 使用用户ID作为Session键，避免不同用户之间的Session冲突
        String sessionKey = "patient_" + user.getId();

        Patient patient = (Patient) session.getAttribute(sessionKey);
        if (patient == null) {
            // 从数据库查询患者信息
            patient = patientService.getByUserId(user.getId());

            // 如果数据库中没有对应的患者记录，创建一个新的
            if (patient == null) {
                synchronized (this) { // 防止并发创建多个患者记录
                    // 双重检查，防止并发问题
                    patient = patientService.getByUserId(user.getId());
                    if (patient == null) {
                        patient = new Patient();
                        patient.setUserId(user.getId());

                        // 设置患者姓名
                        if (user.getRealName() != null && !user.getRealName().trim().isEmpty()) {
                            patient.setPatientName(user.getRealName());
                        } else if (user.getUsername() != null) {
                            patient.setPatientName(user.getUsername());
                        } else {
                            patient.setPatientName("患者" + user.getId());
                        }

                        // 设置默认值
                        patient.setGender("未知");
                        patient.setIdCard("");
                        patient.setAddress("");
                        patient.setEmergencyContact("");
                        patient.setEmergencyPhone("");
                        patient.setMedicalHistory("");
                        patient.setAllergyHistory("");
                        // birthDate保持null，age会自动计算

                        try {
                            // ✅ 使用你新增的savePatient方法（返回Patient对象）
                            patient = patientService.savePatient(patient);

                            log.info("为新用户创建患者记录 - 用户ID: {}, 患者ID: {}, 姓名: {}",
                                    user.getId(), patient.getId(), patient.getPatientName());
                        } catch (Exception e) {
                            log.error("创建患者记录失败 - 用户ID: {}", user.getId(), e);
                            throw new RuntimeException("无法创建患者信息，请稍后重试");
                        }
                    }
                }
            } else {
                // 验证患者记录确实属于当前用户（安全校验）
                if (patient.getUserId() != null && !patient.getUserId().equals(user.getId())) {
                    log.warn("患者记录用户ID不匹配 - 患者ID: {}, 患者用户ID: {}, 当前用户ID: {}",
                            patient.getId(), patient.getUserId(), user.getId());
                    // 清除错误数据，重新创建
                    patient = null;
                    session.removeAttribute(sessionKey);
                    throw new SecurityException("患者信息验证失败，请联系管理员");
                }
            }

            // 将患者信息存入session（使用用户ID作为key）
            if (patient != null) {
                session.setAttribute(sessionKey, patient);
            }
        }

        return patient;
    }

    /**
     * 显示病人登录页面
     */
    @GetMapping("/patient/login")
    public String showPatientLoginPage() {
        return "redirect:/login";
    }

    /**
     * 处理病人登录
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        // 这个方法现在只是一个转发，实际的认证由Spring Security处理
        // 如果到达这里，说明认证失败
        model.addAttribute("error", "用户名或密码错误");
        return "redirect:/login";
    }
    /**
     * 显示病人注册页面
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "patient/register";
    }

    /**
     * 忘记密码（患者端，未登录）
     */
    @PostMapping("/forgot-password")
    @ResponseBody
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> req) {
        Map<String, Object> result = new HashMap<>();
        String identifier = req.get("identifier");
        String newPassword = req.get("newPassword");
        String confirmPassword = req.get("confirmPassword");
        if (identifier == null || identifier.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入用户名或手机号");
            return result;
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入新密码");
            return result;
        }
        if (!newPassword.equals(confirmPassword)) {
            result.put("success", false);
            result.put("message", "两次输入的密码不一致");
            return result;
        }
        User user = userService.findByUsername(identifier);
        if (user == null) {
            user = userService.findByPhone(identifier);
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "未找到患者账号");
            return result;
        }
        boolean ok = userService.updatePassword(user.getId(), newPassword);
        if (ok) {
            result.put("success", true);
            result.put("message", "密码已重置，请使用新密码登录");
        } else {
            result.put("success", false);
            result.put("message", "重置失败，请稍后重试");
        }
        return result;
    }
    /**
     * 处理病人注册
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String realName,
            @RequestParam String gender,
            @RequestParam String idCard,
            HttpSession session,
            Model model) {

        // 验证密码一致性
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "patient/register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRealName(realName);
        user.setUserType("PATIENT");
        user.setCreatedByAdmin(false);

        boolean registered = userService.register(user);
        if (registered) {
            // 注册成功后创建病人信息
            User savedUser = userService.findByUsername(username);
            Patient patient = new Patient();
            patient.setUserId(savedUser.getId());
            patient.setGender(gender);
            patient.setIdCard(idCard);
            patientService.save(patient);

            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("error", "用户名或电话号码已存在");
            return "patient/register";
        }
    }

    /**
     * 病人仪表板
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("User in dashboard: " + user.getUsername() + ", type: " + user.getUserType());

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Patient in dashboard: " + patient);
        System.out.println("Patient ID: " + (patient != null ? patient.getId() : "null"));

        // ============ 获取统计数据 ============
        int medicalRecordCount = 0;
        int appointmentCount = 0;
        int diagnosisCount = 0;
        int medicationCount = 0;
        int examinationCount = 0;

        // 获取预约列表
        List<Appointment> appointments = new ArrayList<>();

        if (patient != null && patient.getId() != null) {
            System.out.println("开始查询统计数据，患者ID: " + patient.getId());

            // 获取病人的病历数量
            medicalRecordCount = medicalRecordService.countByPatientId(patient.getId());
            System.out.println("病历数量查询结果: " + medicalRecordCount);

            // 获取病人的预约数量
            appointmentCount = appointmentService.countByPatientId(patient.getId());
            System.out.println("预约数量查询结果: " + appointmentCount);

            // 诊断记录等于电子病历数量
            diagnosisCount = medicalRecordCount;  // 关键修改：诊断记录等于电子病历数量
            System.out.println("诊断记录数量(等于病历数量): " + diagnosisCount);

            // 用药记录等于电子病历数量
            medicationCount = medicalRecordCount;  // 关键修改：用药记录等于电子病历数量
            System.out.println("用药记录数量(等于病历数量): " + medicationCount);

            // 获取病人的检查报告数量
            examinationCount = examinationService.countByPatientId(patient.getId());
            System.out.println("检查报告数量查询结果: " + examinationCount);

            // ============ 获取预约列表（按时间倒序排序） ============
            appointments = appointmentService.getByPatientId(patient.getId());
            System.out.println("预约列表数量: " + appointments.size());

            // 按预约日期倒序排序（最新的在前面）
            appointments.sort((a1, a2) -> {
                if (a2.getAppointmentDate() == null) return -1;
                if (a1.getAppointmentDate() == null) return 1;
                return a2.getAppointmentDate().compareTo(a1.getAppointmentDate());
            });

            // 为每个预约补充医生名称和科室名称
            for (Appointment appointment : appointments) {
                if (appointment.getDoctorId() != null) {
                    Doctor doctor = doctorService.getById(appointment.getDoctorId());
                    if (doctor != null) {
                        // 从关联的用户表获取医生姓名
                        User doctorUser = userService.getById(doctor.getUserId());
                        if (doctorUser != null) {
                            appointment.setDoctorName(doctorUser.getRealName());
                        }

                        // 获取科室信息
                        if (doctor.getDepartmentId() != null) {
                            Department department = departmentService.getById(doctor.getDepartmentId());
                            if (department != null) {
                                appointment.setDepartmentName(department.getName());
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("患者信息为空或患者ID为空，无法查询统计数据");
        }

        // 调试输出统计数据
        System.out.println("最终统计数量 - 病历: " + medicalRecordCount +
                ", 预约: " + appointmentCount +
                ", 诊断: " + diagnosisCount +
                ", 用药: " + medicationCount +
                ", 检查: " + examinationCount);

        // ============ 获取病人的血压数据 ============
        List<Double> systolicData = new ArrayList<>();
        List<Double> diastolicData = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();

        if (patient != null && patient.getId() != null) {
            // 获取月度血压数据
            List<MonthlyBloodPressureDTO> monthlyBloodPressure =
                    healthIndicatorService.getMonthlyBloodPressureByPatientId(patient.getId());

            if (monthlyBloodPressure != null && !monthlyBloodPressure.isEmpty()) {
                for (MonthlyBloodPressureDTO dto : monthlyBloodPressure) {
                    systolicData.add(dto.getAvgSystolic() != null ? dto.getAvgSystolic() : 0.0);
                    diastolicData.add(dto.getAvgDiastolic() != null ? dto.getAvgDiastolic() : 0.0);
                    monthLabels.add(dto.getYearMonth());
                }
            }
        }

        // 调试输出
        System.out.println("血压数据 - systolicData: " + systolicData);
        System.out.println("血压数据 - diastolicData: " + diastolicData);
        System.out.println("预约列表数量: " + appointments.size());

        // ============ 将所有数据添加到模型 ============
        // 血压数据
        model.addAttribute("systolicData", systolicData);
        model.addAttribute("diastolicData", diastolicData);
        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("currentYear", LocalDate.now().getYear());

        // 用户和患者信息
        model.addAttribute("user", user);
        model.addAttribute("patient", patient);

        // 统计数据 - 确保这些值被添加到模型
        model.addAttribute("medicalRecordCount", medicalRecordCount);
        model.addAttribute("appointmentCount", appointmentCount);
        model.addAttribute("diagnosisCount", diagnosisCount);
        model.addAttribute("medicationCount", medicationCount);
        model.addAttribute("examinationCount", examinationCount);

        // 预约列表
        model.addAttribute("appointments", appointments);

        // 添加一个标志，表示数据已加载
        model.addAttribute("dataLoaded", true);

        System.out.println("模型属性已设置，将返回 patient/dashboard 视图");
        return "patient/dashboard";
    }

    /**
     * 获取血压数据的API接口（用于AJAX刷新）
     */
    @GetMapping("/blood-pressure-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBloodPressureData(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("message", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "用户类型不匹配");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = (Patient) session.getAttribute("patient");
        if (patient == null) {
            patient = patientService.getByUserId(user.getId());
            if (patient != null) {
                session.setAttribute("patient", patient);
            }
        }

        if (patient == null) {
            result.put("success", false);
            result.put("message", "患者信息不存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        // ============ 获取血压数据 ============
        int months = 12;
        List<Double> systolicData = new ArrayList<>();
        List<Double> diastolicData = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();

        // 生成最近12个月的标签
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            java.time.LocalDate date = today.minusMonths(i);
            String yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            monthLabels.add(yearMonth);
        }

        // 初始化数据数组
        for (int i = 0; i < months; i++) {
            systolicData.add(0.0);
            diastolicData.add(0.0);
        }

        // 获取月度血压数据
        if (patient.getId() != null) {
            List<MonthlyBloodPressureDTO> monthlyBloodPressure =
                    healthIndicatorService.getMonthlyBloodPressureByPatientId(patient.getId());

            if (monthlyBloodPressure != null && !monthlyBloodPressure.isEmpty()) {
                for (MonthlyBloodPressureDTO dto : monthlyBloodPressure) {
                    String yearMonth = dto.getYearMonth();
                    int index = monthLabels.indexOf(yearMonth);
                    if (index >= 0) {
                        if (dto.getAvgSystolic() != null) {
                            systolicData.set(index, dto.getAvgSystolic());
                        }
                        if (dto.getAvgDiastolic() != null) {
                            diastolicData.set(index, dto.getAvgDiastolic());
                        }
                    }
                }
            }
        }

        // 调试输出
        System.out.println("API返回血压数据:");
        for (int i = 0; i < monthLabels.size(); i++) {
            System.out.println(monthLabels.get(i) +
                    ": 收缩压=" + systolicData.get(i) +
                    ", 舒张压=" + diastolicData.get(i));
        }

        result.put("success", true);
        result.put("systolicData", systolicData);
        result.put("diastolicData", diastolicData);
        result.put("monthLabels", monthLabels);
        result.put("currentYear", today.getYear());

        return ResponseEntity.ok(result);
    }

    /**
     * AI健康助手页面
     */
    @GetMapping("/ai-assistant")
    public String aiAssistant(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("User in AI assistant: " + user.getUsername() + ", type: " + user.getUserType());

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Patient in AI assistant: " + patient);

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        return "patient/ai-assistant";
    }

    /**
     * 个人信息管理页面
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        Patient patient = (Patient) session.getAttribute("patient");
        System.out.println("Profile page - Patient in session: " + patient);
        if (patient == null) {
            patient = patientService.getByUserId(user.getId());
            System.out.println("Profile page - Patient from DB: " + patient);
            // 如果数据库中没有对应的患者记录，创建一个新的
            if (patient == null) {
                patient = new Patient();
                patient.setUserId(user.getId());
                patient.setGender("未知");
                patientService.save(patient);
                System.out.println("Profile page - Created new patient: " + patient);
            }
            // 将患者信息存入session
            session.setAttribute("patient", patient);
            System.out.println("Profile page - Patient stored in session");
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    /**
     * AJAX更新个人信息
     */
    @PostMapping("/profile/update-ajax")
    @ResponseBody
    public Map<String, Object> updateProfileAjax(
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String birthDay,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("message", "未登录或会话已过期，请重新登录");
            return result;
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "用户不存在或权限不足");
            return result;
        }

        try {
            // 验证输入数据
            Map<String, String> fieldErrors = new HashMap<>();

            // 验证真实姓名
            if (realName != null && realName.trim().length() > 50) {
                fieldErrors.put("realName", "真实姓名不能超过50个字符");
            }

            // 验证手机号格式
            if (phone != null) {
                phone = phone.trim();
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    fieldErrors.put("phone", "请输入有效的11位手机号码");
                }
            }

            // 验证邮箱格式
            if (email != null) {
                email = email.trim();
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    fieldErrors.put("email", "请输入有效的邮箱地址");
                }
            }

            // 新增：验证生日格式
            if (birthDay != null && !birthDay.isEmpty()) {
                try {
                    // 尝试解析日期，验证格式是否正确
                    LocalDate.parse(birthDay);
                } catch (Exception e) {
                    fieldErrors.put("birthDay", "生日格式不正确，请选择有效的日期");
                }
            }

            // 如果有字段错误，返回错误信息
            if (!fieldErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", "表单数据验证失败");
                result.put("fieldErrors", fieldErrors);
                return result;
            }

            // 更新用户信息
            if (realName != null) user.setRealName(realName.trim());
            if (phone != null) user.setPhone(phone);
            if (email != null) user.setEmail(email);
            if (birthDay != null && !birthDay.isEmpty()) {
                user.setBirthDay(birthDay);
            } else {
                user.setBirthDay(null); // 空值时置为null
            }

            // 调用服务层更新
            boolean updateSuccess = userService.updateById(user);

            if (updateSuccess) {
                // 更新session中的用户信息
                session.setAttribute("user", user);

                // 如果患者信息也存在，更新患者信息
                Patient patient = (Patient) session.getAttribute("patient");
                if (patient == null) {
                    patient = patientService.getByUserId(user.getId());
                }
                if (patient != null) {
                    // 同步更新患者的出生日期
                    if (birthDay != null && !birthDay.isEmpty()) {
                        patient.setBirthDate(LocalDate.parse(birthDay));
                    } else {
                        patient.setBirthDate(null);
                    }
                    patientService.updateById(patient);
                    session.setAttribute("patient", patient);
                }

                result.put("success", true);
                result.put("message", "个人信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "数据库更新失败，请稍后重试");
            }
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器内部错误：" + e.getMessage());
        }

        return result;
    }


    /**
     * 诊断记录查询页面
     */
    @GetMapping("/diagnoses")
    public String diagnoses(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Diagnoses page - User in session: " + user);
        if (user != null) {
            System.out.println("Diagnoses page - User type: " + user.getUserType());
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Diagnoses page - Patient: " + patient);

        // 获取患者的诊断记录
        List<Diagnosis> diagnoses = new ArrayList<>();
        if (patient.getId() != null) {
            diagnoses = diagnosisService.getByPatientId(patient.getId());
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("diagnoses", diagnoses);
        return "patient/diagnoses";
    }

    /**
     * 检查报告查询页面
     */
    @GetMapping("/examinations")
    public String examinations(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Examinations page - User in session: " + user);
        if (user != null) {
            System.out.println("Examinations page - User type: " + user.getUserType());
        }

        Patient patient = (Patient) session.getAttribute("patient");
        System.out.println("Examinations page - Patient in session: " + patient);
        if (patient == null) {
            patient = patientService.getByUserId(user.getId());
            System.out.println("Examinations page - Patient from DB: " + patient);
        }

        // 获取患者的检查报告
        List<Examination> examinations = new ArrayList<>();
        if (patient.getId() != null) {
            examinations = examinationService.getByPatientId(patient.getId());
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("examinations", examinations);
        return "patient/examinations";
    }

    /**
     * 用药历史查询页面
     */
    @GetMapping("/medications")
    public String medications(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Medications page - User in session: " + user);
        if (user != null) {
            System.out.println("Medications page - User type: " + user.getUserType());
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Medications page - Patient: " + patient);

        // 获取患者的用药记录
        List<Medication> medications = new ArrayList<>();
        if (patient.getId() != null) {
            medications = medicationService.getByPatientId(patient.getId());
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("medications", medications);
        return "patient/medications";
    }

    /**
     * 预约挂号页面
     */
    @GetMapping("/appointments/book")
    public String bookAppointment(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Book appointment page - User in session: " + user);
        if (user != null) {
            System.out.println("Book appointment page - User type: " + user.getUserType());
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Book appointment page - Patient: " + patient);

        // 获取所有科室和医生
        List<Department> departments = departmentService.list();
        List<Doctor> doctors = doctorService.list();

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("departments", departments);
        model.addAttribute("doctors", doctors);
        return "patient/book-appointment";
    }

    /**
     * 处理预约挂号
     */
    @PostMapping("/appointments/book")
    public String processBookAppointment(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String timeSlot,
            HttpSession session,
            Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setTimeSlot(timeSlot);
        appointment.setAppointmentTime(LocalDateTime.now());
        appointment.setStatus("待确认");

        boolean saved = appointmentService.save(appointment);
        if (saved) {
            return "redirect:/patient/appointments?booked=true";
        } else {
            model.addAttribute("error", "预约失败，请重试");
            return "patient/book-appointment";
        }
    }

    /**
     * 预约列表页面（我的预约 - 显示当前未取消的预约）
     */
    @GetMapping("/appointments")
    public String appointments(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Appointments page - User in session: " + user);
        if (user != null) {
            System.out.println("Appointments page - User type: " + user.getUserType());
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Appointments page - Patient: " + patient);

        // 获取病人的所有预约（包括已取消的）
        List<Appointment> appointments = new ArrayList<>();
        if (patient.getId() != null) {
            appointments = appointmentService.getByPatientId(patient.getId());

            // 为每个预约补充医生名称和科室名称
            for (Appointment appointment : appointments) {
                if (appointment.getDoctorId() != null) {
                    Doctor doctor = doctorService.getById(appointment.getDoctorId());
                    if (doctor != null) {
                        // 从关联的用户表获取医生姓名
                        User doctorUser = userService.getById(doctor.getUserId());
                        if (doctorUser != null) {
                            appointment.setDoctorName(doctorUser.getRealName());
                        }

                        // 获取科室信息
                        if (doctor.getDepartmentId() != null) {
                            Department department = departmentService.getById(doctor.getDepartmentId());
                            if (department != null) {
                                appointment.setDepartmentName(department.getName());
                            }
                        }
                    }
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        return "patient/appointments";
    }

    /**
     * 医生排班查询页面
     */
    @GetMapping("/schedules")
    public String schedules(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        System.out.println("Schedules page - User in session: " + user);
        if (user != null) {
            System.out.println("Schedules page - User type: " + user.getUserType());
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Schedules page - Patient: " + patient);

        // 获取所有医生排班信息（这里简化处理，实际应该有排班表）
        List<Doctor> doctors = doctorService.list();

        // 为每个医生补充科室名称
        for (Doctor doctor : doctors) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentService.getById(doctor.getDepartmentId());
                if (department != null) {
                    doctor.setDepartmentName(department.getName());
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("doctors", doctors);
        return "patient/schedules";
    }

    /**
     * 取消预约
     */
    @PostMapping("/appointments/cancel/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id, HttpSession session) {
        // 1. 调试日志：打印关键参数
        System.out.println("取消预约请求 - ID：" + id);
        System.out.println("Session中的Patient：" + session.getAttribute("patient"));

        Map<String, String> result = new HashMap<>();

        // 2. 修复Patient获取逻辑（兼容Security认证）
        Patient patient = (Patient) session.getAttribute("patient");
        if (patient == null) {
            // 兜底：从Security上下文重新获取用户并创建Patient
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || "anonymousUser".equals(auth.getName())) {
                result.put("msg", "未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                result.put("msg", "用户不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
            patient = patientService.getByUserId(user.getId());
            if (patient == null) {
                result.put("msg", "患者信息不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
            // 重新存入Session
            session.setAttribute("patient", patient);
        }

        // 3. 查询预约并校验
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            result.put("msg", "预约记录不存在");
            System.out.println("取消失败：预约ID " + id + " 不存在");
            return ResponseEntity.badRequest().body(result);
        }

        if (!patient.getId().equals(appointment.getPatientId())) {
            result.put("msg", "无权取消他人的预约");
            System.out.println("取消失败：患者ID " + patient.getId() + " 无权取消预约ID " + id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // 检查预约状态是否允许取消（待确认或已确认）
        if (!"待确认".equals(appointment.getStatus()) && !"已确认".equals(appointment.getStatus())) {
            result.put("msg", "仅可取消「待确认」或「已确认」状态的预约（当前状态：" + appointment.getStatus() + "）");
            System.out.println("取消失败：预约ID " + id + " 状态为 " + appointment.getStatus());
            return ResponseEntity.badRequest().body(result);
        }

        // 检查是否在就诊前24小时内，限制取消
        if (appointment.getAppointmentDate() != null) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    appointment.getAppointmentDate()
            );
            if (daysBetween < 1) {
                result.put("msg", "就诊前24小时内无法取消预约");
                System.out.println("取消失败：预约ID " + id + " 已进入24小时限制期");
                return ResponseEntity.badRequest().body(result);
            }
        }

        // 4. 更新预约状态
        appointment.setStatus("已取消");
        boolean updateSuccess = appointmentService.updateById(appointment);
        if (!updateSuccess) {
            result.put("msg", "数据库更新失败");
            System.out.println("取消失败：预约ID " + id + " 更新数据库失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        // 5. 成功响应
        result.put("msg", "预约取消成功");
        System.out.println("取消成功：预约ID " + id);
        return ResponseEntity.ok(result);
    }

    /**
     * 显示提醒设置页面
     */
    @GetMapping("/appointments/set-reminder/{id}")
    public String showSetReminderPage(@PathVariable Long id, HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);
        if (patient == null) {
            return "redirect:/login";
        }

        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getPatientId().equals(patient.getId())) {
            return "redirect:/patient/appointment-management";
        }

        // 获取医生信息
        Doctor doctor = null;
        if (appointment.getDoctorId() != null) {
            doctor = doctorService.getById(appointment.getDoctorId());
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);
        model.addAttribute("doctor", doctor);
        return "patient/set-reminder";
    }

    /**
     * 设置预约提醒
     */
    @PostMapping("/appointments/set-reminder/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> setReminder(
            @PathVariable Long id,
            @RequestParam(required = false) Integer reminderTimeOffset,
            @RequestParam(required = false) String reminderMethods,
            HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("msg", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("msg", "用户不存在或权限不足");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = getOrCreatePatient(user, session);
        if (patient == null) {
            result.put("msg", "患者信息不存在");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 查询预约并校验
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            result.put("msg", "预约记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        if (!patient.getId().equals(appointment.getPatientId())) {
            result.put("msg", "无权设置他人的预约提醒");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // 设置默认提醒时间（提前2小时=120分钟）
        if (reminderTimeOffset == null) {
            reminderTimeOffset = -120; // 提前2小时
        }

        // 设置默认提醒方式
        if (reminderMethods == null || reminderMethods.trim().isEmpty()) {
            reminderMethods = "站内信"; // 默认提醒方式
        }

        // 更新提醒设置
        appointment.setReminderEnabled(1); // 1表示已设置提醒
        appointment.setReminderTimeOffset(reminderTimeOffset);
        appointment.setReminderMethods(reminderMethods);
        appointment.setLastReminderTime(null); // 重置最后提醒时间

        boolean updated = appointmentService.updateById(appointment);
        if (updated) {
            result.put("msg", "提醒设置成功");
        } else {
            result.put("msg", "提醒设置失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 编辑预约提醒
     */
    @PostMapping("/appointments/edit-reminder/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editReminder(
            @PathVariable Long id,
            @RequestParam Integer reminderTimeOffset,
            @RequestParam String reminderMethods,
            HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("msg", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("msg", "用户不存在或权限不足");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = getOrCreatePatient(user, session);
        if (patient == null) {
            result.put("msg", "患者信息不存在");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 查询预约并校验
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            result.put("msg", "预约记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        if (!patient.getId().equals(appointment.getPatientId())) {
            result.put("msg", "无权编辑他人的预约提醒");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // 检查是否已设置提醒
        if (appointment.getReminderEnabled() == null || appointment.getReminderEnabled() == 0) {
            result.put("msg", "该预约尚未设置提醒，无法编辑");
            return ResponseEntity.badRequest().body(result);
        }

        // 更新提醒设置
        appointment.setReminderTimeOffset(reminderTimeOffset);
        appointment.setReminderMethods(reminderMethods);
        appointment.setLastReminderTime(null); // 重置最后提醒时间以便重新触发

        boolean updated = appointmentService.updateById(appointment);
        if (updated) {
            result.put("msg", "提醒编辑成功");
        } else {
            result.put("msg", "提醒编辑失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 取消预约提醒
     */
    @PostMapping("/appointments/cancel-reminder/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelReminder(@PathVariable Long id, HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("msg", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("msg", "用户不存在或权限不足");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = getOrCreatePatient(user, session);
        if (patient == null) {
            result.put("msg", "患者信息不存在");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 查询预约并校验
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            result.put("msg", "预约记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        if (!patient.getId().equals(appointment.getPatientId())) {
            result.put("msg", "无权取消他人的预约提醒");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // 检查是否已设置提醒
        if (appointment.getReminderEnabled() == null || appointment.getReminderEnabled() == 0) {
            result.put("msg", "该预约尚未设置提醒");
            return ResponseEntity.badRequest().body(result);
        }

        // 取消提醒设置
        appointment.setReminderEnabled(0); // 0表示未设置提醒
        appointment.setReminderTimeOffset(null);
        appointment.setReminderMethods(null);
        appointment.setLastReminderTime(null);

        boolean updated = appointmentService.updateById(appointment);
        if (updated) {
            result.put("msg", "提醒取消成功");
        } else {
            result.put("msg", "提醒取消失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 预约管理页面（显示所有预约记录，包括历史记录）
     */
    @GetMapping("/appointment-management")
    public String appointmentManagement(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Appointment management page - Patient: " + patient);

        // 获取病人的所有预约（包括历史记录）
        List<Appointment> appointments = new ArrayList<>();
        if (patient.getId() != null) {
            appointments = appointmentService.getByPatientId(patient.getId());

            // 为每个预约补充医生名称、科室名称和拒绝原因
            for (Appointment appointment : appointments) {
                if (appointment.getDoctorId() != null) {
                    Doctor doctor = doctorService.getById(appointment.getDoctorId());
                    if (doctor != null) {
                        // 从关联的用户表获取医生姓名
                        User doctorUser = userService.getById(doctor.getUserId());
                        if (doctorUser != null) {
                            appointment.setDoctorName(doctorUser.getRealName());
                        }

                        // 获取科室信息
                        if (doctor.getDepartmentId() != null) {
                            Department department = departmentService.getById(doctor.getDepartmentId());
                            if (department != null) {
                                appointment.setDepartmentName(department.getName());
                            }
                        }
                    }
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointments);
        return "patient/appointment-management";
    }

    /**
     * 修改预约页面
     */
    @GetMapping("/appointments/edit/{id}")
    public String editAppointmentForm(@PathVariable Long id, HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);

        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getPatientId().equals(patient.getId())) {
            return "redirect:/patient/appointment-management";
        }

        // 检查是否可以修改（只有待确认状态的预约可以修改）
        if (!"待确认".equals(appointment.getStatus())) {
            return "redirect:/patient/appointment-management?error=only_pending_can_modify";
        }

        // 获取医生信息
        Doctor doctor = doctorService.getById(appointment.getDoctorId());

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("appointment", appointment);
        model.addAttribute("doctor", doctor);
        return "patient/edit-appointment";
    }

    /**
     * 处理修改预约请求
     */
    @PostMapping("/appointments/edit/{id}")
    public String processEditAppointment(
            @PathVariable Long id,
            @RequestParam String appointmentDate,
            @RequestParam String timeSlot,
            @RequestParam String reason,
            @RequestParam(required = false) String pastIllness,
            HttpSession session,
            Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);

        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getPatientId().equals(patient.getId())) {
            return "redirect:/patient/appointment-management";
        }

        // 检查是否可以修改（只有待确认状态的预约可以修改）
        if (!"待确认".equals(appointment.getStatus())) {
            return "redirect:/patient/appointment-management?error=only_pending_can_modify";
        }

        // 更新预约信息
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setTimeSlot(timeSlot);
        appointment.setReason(reason);
        appointment.setNotes(pastIllness);

        boolean updated = appointmentService.updateById(appointment);
        if (updated) {
            return "redirect:/patient/appointment-management?updated=true";
        } else {
            model.addAttribute("error", "修改失败，请重试");
            return "patient/edit-appointment";
        }
    }

    /**
     * 预约医生页面
     */
    @GetMapping("/appointments/make")
    public String makeAppointmentForm(@RequestParam Long doctorId, HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            System.out.println("User type mismatch or user not found, redirecting to login page");
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);
        System.out.println("Make appointment page - Patient: " + patient);

        // 获取医生信息
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return "redirect:/patient/schedules";
        }

        // 获取医生姓名（从关联的User表）
        String doctorName = "";
        if (doctor.getUserId() != null) {
            User doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                doctorName = doctorUser.getRealName();
            }
        }

        // 获取科室名称
        String departmentName = "";
        if (doctor.getDepartmentId() != null) {
            Department department = departmentService.getById(doctor.getDepartmentId());
            if (department != null) {
                departmentName = department.getName();
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor);
        model.addAttribute("doctorName", doctorName);       // 医生姓名
        model.addAttribute("departmentName", departmentName); // 科室名称
        return "patient/make-appointment";
    }


    /**
     * AJAX处理预约医生请求
     */
    @PostMapping("/appointments/make-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processMakeAppointmentAjax(
            @RequestBody Map<String, Object> requestData,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("msg", "未登录，请先登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("success", false);
            result.put("msg", "用户不存在或权限不足");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = getOrCreatePatient(user, session);

        try {
            // 获取请求参数
            Long doctorId = null;
            if (requestData.get("doctorId") != null) {
                try {
                    doctorId = Long.parseLong(requestData.get("doctorId").toString());
                } catch (NumberFormatException e) {
                    result.put("success", false);
                    result.put("msg", "医生ID格式错误");
                    return ResponseEntity.badRequest().body(result);
                }
            }

            String appointmentDate = (String) requestData.get("appointmentDate");
            String timeSlot = (String) requestData.get("timeSlot");
            String reason = (String) requestData.get("reason");
            String pastIllness = (String) requestData.get("pastIllness");

            // 验证参数
            if (doctorId == null) {
                result.put("success", false);
                result.put("msg", "请选择医生");
                return ResponseEntity.badRequest().body(result);
            }

            if (appointmentDate == null || appointmentDate.trim().isEmpty()) {
                result.put("success", false);
                result.put("msg", "请选择预约日期");
                return ResponseEntity.badRequest().body(result);
            }

            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                result.put("success", false);
                result.put("msg", "请选择时间段");
                return ResponseEntity.badRequest().body(result);
            }

            if (reason == null || reason.trim().isEmpty()) {
                result.put("success", false);
                result.put("msg", "请输入病症描述");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证医生是否存在
            Doctor doctor = doctorService.getById(doctorId);
            if (doctor == null) {
                result.put("success", false);
                result.put("msg", "医生不存在");
                return ResponseEntity.badRequest().body(result);
            }

            // ============ 新增：检查号源是否还有剩余 ============
            LocalDate date = LocalDate.parse(appointmentDate);

            // 查询该医生在指定日期的排班记录
            Map<String, Object> schedule = doctorService.getDoctorSchedule(doctorId, date, timeSlot);

            if (schedule == null) {
                result.put("success", false);
                result.put("msg", "该时段暂无排班");
                return ResponseEntity.badRequest().body(result);
            }

            // 获取总号数和已预约数
            Integer quota = (Integer) schedule.get("registration_quota");
            Integer registered = (Integer) schedule.get("registered_count");

            if (quota == null) quota = 0;
            if (registered == null) registered = 0;

            // 检查是否还有剩余号
            if (registered >= quota) {
                result.put("success", false);
                result.put("msg", "该时段已约满，请选择其他时段");
                return ResponseEntity.badRequest().body(result);
            }
            // ============ 检查结束 ============

            // 创建预约
            Appointment appointment = new Appointment();
            appointment.setPatientId(patient.getId());
            appointment.setDoctorId(doctorId);
            appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
            appointment.setTimeSlot(timeSlot);
            appointment.setAppointmentTime(LocalDateTime.now());
            appointment.setStatus("待确认");
            appointment.setReason(reason);
            appointment.setNotes(pastIllness);

            boolean saved = appointmentService.save(appointment);
            if (saved) {
                // ============ 新增：预约成功后，将 registered_count 加 1 ============
                boolean updated = doctorService.incrementRegisteredCount(doctorId, date, timeSlot);
                if (!updated) {
                    // 如果更新失败，记录日志但不影响预约成功
                    System.err.println("警告：更新预约人数失败，医生ID：" + doctorId + "，日期：" + date + "，时间段：" + timeSlot);
                }
                // ============ 更新结束 ============

                result.put("success", true);
                result.put("msg", "预约成功！医生会在24小时内确认您的预约");

                System.out.println("患者 " + user.getRealName() + " 成功预约了医生 " + doctorId);
            } else {
                result.put("success", false);
                result.put("msg", "预约失败，请稍后重试");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "系统错误：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }




    /**
     * 处理预约医生请求
     */
    @PostMapping("/appointments/make")
    public String processMakeAppointment(
            @RequestParam Long doctorId,
            @RequestParam String appointmentDate,
            @RequestParam String timeSlot,
            @RequestParam String reason,
            @RequestParam(required = false) String pastIllness,
            HttpSession session,
            Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);

        // 验证医生是否存在
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            model.addAttribute("error", "医生不存在");
            return "patient/make-appointment";
        }

        // 创建预约
        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(LocalDate.parse(appointmentDate));
        appointment.setTimeSlot(timeSlot);
        appointment.setAppointmentTime(LocalDateTime.now());
        appointment.setStatus("待确认");
        appointment.setReason(reason);
        appointment.setNotes(pastIllness);

        boolean saved = appointmentService.save(appointment);
        if (saved) {
            return "redirect:/patient/appointments?booked=true";
        } else {
            model.addAttribute("error", "预约失败，请重试");
            model.addAttribute("doctor", doctor);
            return "patient/make-appointment";
        }
    }

    /**
     * 更新健康档案
     */
    @PostMapping("/health-profile/update")
    public String updateHealthProfile(@ModelAttribute HealthProfile profile, HttpSession session) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = (Patient) session.getAttribute("patient");
        if (patient == null) {
            patient = patientService.getByUserId(user.getId());
        }

        profile.setPatientId(patient.getId());
        healthProfileService.saveOrUpdate(profile);

        return "redirect:/patient/health-profile?updated=true";
    }

    /**
     * 获取预约详情（用于弹窗显示）
     */
    @GetMapping("/appointments/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAppointmentDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("msg", "未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            result.put("success", false);
            result.put("msg", "用户不存在或不是患者");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        Patient patient = (Patient) session.getAttribute("patient");
        if (patient == null) {
            patient = patientService.getByUserId(user.getId());
            if (patient != null) {
                session.setAttribute("patient", patient);
            }
        }

        if (patient == null) {
            result.put("success", false);
            result.put("msg", "患者信息不存在");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            result.put("success", false);
            result.put("msg", "预约记录不存在");
            return ResponseEntity.badRequest().body(result);
        }

        // 验证是否是当前用户的预约
        if (!appointment.getPatientId().equals(patient.getId())) {
            result.put("success", false);
            result.put("msg", "无权查看该预约");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        // 获取医生信息
        Doctor doctor = null;
        User doctorUser = null;
        if (appointment.getDoctorId() != null) {
            doctor = doctorService.getById(appointment.getDoctorId());
            if (doctor != null && doctor.getUserId() != null) {
                doctorUser = userService.getById(doctor.getUserId());
            }
        }

        // 获取科室信息
        Department department = null;
        if (doctor != null && doctor.getDepartmentId() != null) {
            department = departmentService.getById(doctor.getDepartmentId());
        }

        // 构建返回数据
        Map<String, Object> appointmentDetail = new HashMap<>();
        appointmentDetail.put("id", appointment.getId());
        appointmentDetail.put("appointmentDate", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
        appointmentDetail.put("appointmentTime", appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "");
        appointmentDetail.put("timeSlot", appointment.getTimeSlot());
        appointmentDetail.put("status", appointment.getStatus());
        appointmentDetail.put("reason", appointment.getReason());
        appointmentDetail.put("notes", appointment.getNotes());
        appointmentDetail.put("createdTime", appointment.getCreatedTime() != null ? appointment.getCreatedTime().toString() : "");

        if (doctorUser != null) {
            appointmentDetail.put("doctorName", doctorUser.getRealName());
        }
        if (doctor != null) {
            appointmentDetail.put("doctorTitle", doctor.getTitle());
            appointmentDetail.put("doctorSpecialty", doctor.getSpecialty());
        }

        if (department != null) {
            appointmentDetail.put("departmentName", department.getName());
        }

        result.put("success", true);
        result.put("appointment", appointmentDetail);

        return ResponseEntity.ok(result);
    }

    /**
     * 病人退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }


    @GetMapping("/patient/finddoctors")
    public String findDoctors() {
        // 直接返回视图名（无需重定向），假设你的视图文件是 finddoctors.html/thymeleaf模板
        return "patient/finddoctors";
    }

    /**
     * 头像上传处理（修复版 - 确保静态资源可访问）
     */

    @PostMapping("/avatar/upload")
    @ResponseBody
    public Map<String, Object> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("message", "未登录或会话已过期，请重新登录");
                return result;
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("message", "用户不存在或权限不足");
                return result;
            }

            // 检查文件是否为空
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的文件");
                return result;
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只能上传图片文件");
                return result;
            }

            // 验证文件大小（2MB）
            long fileSize = file.getSize();
            if (fileSize > 2 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "文件大小不能超过2MB");
                return result;
            }

            // 生成存储路径（使用相对路径）
            String uploadDir = "uploads/avatars/patient_" + user.getId();
            String projectRoot = System.getProperty("user.dir");
            String uploadPath = projectRoot + "/" + uploadDir;

            // 确保目录存在
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    result.put("success", false);
                    result.put("message", "创建目录失败");
                    return result;
                }
                System.out.println("创建目录：" + uploadPath);
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = Paths.get(uploadPath, uniqueFileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("文件保存到：" + filePath.toString());
            } catch (IOException e) {
                result.put("success", false);
                result.put("message", "文件保存失败：" + e.getMessage());
                return result;
            }

            // 构建前端访问的URL路径
            // 格式：/uploads/avatars/patient_X/filename.jpg
            String relativePath = "/" + uploadDir + "/" + uniqueFileName;

            // 更新数据库中的头像路径
            boolean updateSuccess = userService.updateAvatar(user.getId(), relativePath);

            if (updateSuccess) {
                // 更新session中的用户信息
                user.setAvatar(relativePath);
                session.setAttribute("user", user);

                result.put("success", true);
                result.put("message", "头像上传成功");
                // 返回前端可以直接使用的URL
                result.put("avatarUrl", relativePath);

                System.out.println("头像上传成功，URL：" + relativePath);
                System.out.println("文件物理路径：" + filePath.toAbsolutePath());
            } else {
                // 删除已上传的文件
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("删除文件失败：" + e.getMessage());
                }
                result.put("success", false);
                result.put("message", "头像保存失败，请重试");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }



    /**
     * 获取当前用户头像（修复版）
     */
    @GetMapping("/avatar/get")
    @ResponseBody
    public Map<String, Object> getAvatar(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("message", "未登录");
                return result;
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("message", "用户不存在或权限不足");
                return result;
            }

            // 检查是否有头像
            String avatarUrl = user.getAvatar();
            System.out.println("获取用户头像，用户ID：" + user.getId() + "，头像路径：" + avatarUrl);

            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                // 如果没有头像，返回默认信息
                result.put("hasAvatar", false);
                // 生成默认头像（使用姓名首字母）
                String defaultAvatar = generateDefaultAvatar(user.getRealName());
                result.put("defaultAvatar", defaultAvatar);
                result.put("realName", user.getRealName());
            } else {
                result.put("hasAvatar", true);

                // 检查文件是否存在
                String projectRoot = System.getProperty("user.dir");
                String filePath = projectRoot + avatarUrl;
                File file = new File(filePath);

                if (file.exists()) {
                    System.out.println("头像文件存在：" + filePath);
                    // 返回完整的URL路径，确保前端能正确访问
                    // 注意：这里返回的是相对路径，WebConfig会映射为静态资源
                    result.put("avatarUrl", avatarUrl);
                    result.put("realName", user.getRealName());
                } else {
                    System.out.println("头像文件不存在：" + filePath);
                    // 文件不存在，返回默认头像
                    result.put("hasAvatar", false);
                    String defaultAvatar = generateDefaultAvatar(user.getRealName());
                    result.put("defaultAvatar", defaultAvatar);
                    result.put("realName", user.getRealName());

                    // 清理数据库中的无效路径
                    userService.updateAvatar(user.getId(), null);
                }
            }

            result.put("success", true);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取头像失败：" + e.getMessage());
            return result;
        }
    }
    /**
     * 生成默认头像（姓名首字母）
     */
    private String generateDefaultAvatar(String realName) {
        if (realName != null && !realName.trim().isEmpty()) {
            return realName.trim().substring(0, 1).toUpperCase();
        }
        return "U"; // 默认返回"U"
    }


    @GetMapping("/profile/data")
    @ResponseBody
    public Map<String, Object> getProfileData(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("message", "未登录或会话已过期，请重新登录");
                return result;
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("message", "用户不存在或权限不足");
                return result;
            }

            // 更新session中的用户信息
            session.setAttribute("user", user);

            // 获取患者信息
            Patient patient = getOrCreatePatient(user, session);
            if (patient != null) {
                result.put("patient", patient);
            }

            result.put("success", true);
            result.put("user", user);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取用户信息失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 重置密码（AJAX接口）
     */
    @PostMapping("/reset-password")
    @ResponseBody
    public Map<String, Object> resetPassword(
            @RequestBody Map<String, String> requestData,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("message", "未登录或会话已过期，请重新登录");
                return result;
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("message", "用户不存在或权限不足");
                return result;
            }

            // 获取请求参数
            String currentPassword = requestData.get("currentPassword");
            String newPassword = requestData.get("newPassword");
            String confirmPassword = requestData.get("confirmPassword");

            // 验证参数
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "当前密码不能为空");
                return result;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新密码不能为空");
                return result;
            }

            if (newPassword.length() < 6) {
                result.put("success", false);
                result.put("message", "新密码长度至少6位");
                return result;
            }

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "确认密码不能为空");
                return result;
            }

            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "两次输入的新密码不一致");
                return result;
            }

            if (currentPassword.equals(newPassword)) {
                result.put("success", false);
                result.put("message", "新密码不能与当前密码相同");
                return result;
            }

            // 验证当前密码是否正确
            boolean isCurrentPasswordCorrect = userService.verifyPassword(user.getId(), currentPassword);
            if (!isCurrentPasswordCorrect) {
                result.put("success", false);
                result.put("message", "当前密码错误");
                return result;
            }

            // 更新密码
            boolean updateSuccess = userService.updatePassword(user.getId(), newPassword);
            if (updateSuccess) {
                result.put("success", true);
                result.put("message", "密码修改成功");

                // 清除session，强制重新登录
                session.invalidate();
            } else {
                result.put("success", false);
                result.put("message", "密码修改失败，请稍后重试");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }

        return result;
    }
    /**
     * 处理设置提醒（前端弹窗提交）
     */
    @PostMapping("/appointments/set-reminder-ajax")
    @ResponseBody
    public Map<String, Object> setReminderAjax(
            @RequestBody Map<String, Object> requestData,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("msg", "未登录或会话已过期，请重新登录");
                return result;
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("msg", "用户不存在或权限不足");
                return result;
            }

            Patient patient = getOrCreatePatient(user, session);
            if (patient == null) {
                result.put("success", false);
                result.put("msg", "患者信息不存在");
                return result;
            }

            // 获取请求参数 - 修复：确保正确解析数据
            Object appointmentIdObj = requestData.get("appointmentId");
            Object reminderTimeObj = requestData.get("reminderTime");
            Object reminderNotesObj = requestData.get("reminderNotes");

            String appointmentIdStr = appointmentIdObj != null ? appointmentIdObj.toString() : null;
            String reminderTimeStr = reminderTimeObj != null ? reminderTimeObj.toString() : null;
            String reminderNotes = reminderNotesObj != null ? reminderNotesObj.toString() : null;

            // 验证参数
            if (appointmentIdStr == null || appointmentIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("msg", "预约ID不能为空");
                return result;
            }

            if (reminderTimeStr == null || reminderTimeStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("msg", "提醒时间不能为空");
                return result;
            }

            Long appointmentId;
            try {
                appointmentId = Long.parseLong(appointmentIdStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("msg", "预约ID格式错误");
                return result;
            }

            // 查询预约并校验
            Appointment appointment = appointmentService.getById(appointmentId);
            if (appointment == null) {
                result.put("success", false);
                result.put("msg", "预约记录不存在");
                return result;
            }

            if (!patient.getId().equals(appointment.getPatientId())) {
                result.put("success", false);
                result.put("msg", "无权设置他人的预约提醒");
                return result;
            }

            // 检查预约状态是否允许设置提醒（待确认或已确认）
            if (!"待确认".equals(appointment.getStatus()) && !"已确认".equals(appointment.getStatus())) {
                result.put("success", false);
                result.put("msg", "只能为待确认或已确认的预约设置提醒");
                return result;
            }

            // 解析提醒时间 - 修复：添加更多日志
            LocalDateTime reminderDateTime;
            try {
                System.out.println("前端发送的提醒时间字符串: " + reminderTimeStr);

                // 尝试多种格式解析
                if (reminderTimeStr.contains("T")) {
                    // 格式：yyyy-MM-ddTHH:mm
                    reminderDateTime = LocalDateTime.parse(reminderTimeStr);
                } else if (reminderTimeStr.contains(" ")) {
                    // 格式：yyyy-MM-dd HH:mm
                    reminderDateTime = LocalDateTime.parse(reminderTimeStr.replace(" ", "T"));
                } else {
                    throw new Exception("时间格式无法识别: " + reminderTimeStr);
                }

                System.out.println("解析后的提醒时间: " + reminderDateTime);
            } catch (Exception e) {
                System.err.println("解析提醒时间失败: " + e.getMessage());
                result.put("success", false);
                result.put("msg", "提醒时间格式错误，请使用正确的日期时间格式");
                return result;
            }

            // 检查提醒时间是否合理
            LocalDateTime now = LocalDateTime.now();
            if (reminderDateTime.isBefore(now)) {
                result.put("success", false);
                result.put("msg", "提醒时间不能早于当前时间");
                return result;
            }

            // 检查提醒时间是否晚于预约时间
            if (appointment.getAppointmentDate() != null && appointment.getTimeSlot() != null) {
                try {
                    // 假设timeSlot格式为"HH:mm"，将预约日期和时间段组合为LocalDateTime
                    String[] timeParts = appointment.getTimeSlot().split(":");
                    if (timeParts.length >= 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        LocalDateTime appointmentDateTime = appointment.getAppointmentDate()
                                .atTime(hour, minute);

                        if (reminderDateTime.isAfter(appointmentDateTime)) {
                            result.put("success", false);
                            result.put("msg", "提醒时间不能晚于预约时间");
                            return result;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("计算预约时间失败: " + e.getMessage());
                    // 继续执行，不因为这个错误而中断
                }
            }

            // 更新提醒设置
            appointment.setReminderEnabled(1); // 1表示已设置提醒
            appointment.setReminderTime(reminderDateTime);
            appointment.setReminderNotes(reminderNotes);

            // 计算提醒时间偏移量（分钟）
            if (appointment.getAppointmentDate() != null && appointment.getTimeSlot() != null) {
                try {
                    String[] timeParts = appointment.getTimeSlot().split(":");
                    if (timeParts.length >= 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        LocalDateTime appointmentDateTime = appointment.getAppointmentDate()
                                .atTime(hour, minute);

                        long minutesOffset = java.time.Duration.between(reminderDateTime, appointmentDateTime).toMinutes();
                        appointment.setReminderTimeOffset((int)minutesOffset);
                        System.out.println("计算的时间偏移量: " + minutesOffset + "分钟");
                    }
                } catch (Exception e) {
                    System.err.println("计算时间偏移失败: " + e.getMessage());
                    appointment.setReminderTimeOffset(30); // 默认30分钟
                }
            } else {
                appointment.setReminderTimeOffset(30); // 默认30分钟
            }

            appointment.setReminderMethods("弹窗提醒");
            appointment.setLastReminderTime(null); // 重置最后提醒时间

            boolean updated = appointmentService.updateById(appointment);
            if (updated) {
                System.out.println("提醒设置成功，预约ID: " + appointmentId);
                result.put("success", true);
                result.put("msg", "提醒设置成功");
            } else {
                System.err.println("提醒设置失败，预约ID: " + appointmentId);
                result.put("success", false);
                result.put("msg", "提醒设置失败，请稍后重试");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "系统错误：" + e.getMessage());
        }

        return result;
    }

    /**
     * 计算健康状态
     */
    private String calculateHealthStatus(HealthProfile profile) {
        if (profile.getBmi() == null) {
            return "未评估";
        }

        double bmi = profile.getBmi();
        if (bmi < 18.5) {
            return "体重偏瘦";
        } else if (bmi < 24) {
            return "健康状态良好";
        } else if (bmi < 28) {
            return "体重超重";
        } else {
            return "体重肥胖";
        }
    }

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);


    /**
     * 饮食建议页面 - 修复版
     */
    @GetMapping("/nutrition")
    public String nutrition(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            log.warn("User not authenticated, redirecting to login page");
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            log.warn("User type mismatch (not PATIENT) or user not found, username/phone: {}", auth.getName());
            return "redirect:/login";
        }

        log.info("Nutrition page - User info: {}, user type: {}", user, user.getUserType());

        Patient patient = getOrCreatePatient(user, session);
        log.info("Nutrition page - Patient info: {}", patient);

        // 获取健康档案
        List<HealthProfile> profileList = null;
        HealthProfile profile = null;
        if (patient != null && patient.getId() != null) {
            profileList = healthProfileService.getByPatientId(patient.getId());
            if (profileList != null && !profileList.isEmpty()) {
                profile = profileList.get(0);
                log.info("Found health profile for patient {}: {}", patient.getId(), profile);
            } else {
                log.info("No health profile found for patient {}. Creating default profile or using patient data.", patient.getId());
                // 如果没有健康档案，创建一个临时的健康档案对象，从患者信息中填充数据
                profile = new HealthProfile();
                profile.setPatientId(patient.getId());
                profile.setHeight(null); // 先设为null
                profile.setWeight(null);
                profile.setBmi(null);
                profile.setBloodPressure(null);

                // 如果有其他数据来源，可以在这里填充
                // 例如，如果有血压指标数据，可以从healthIndicatorService获取
            }
        }

        // 获取血压数据
        List<MonthlyBloodPressureDTO> monthlyBloodPressure = null;
        if (patient != null && patient.getId() != null) {
            monthlyBloodPressure = healthIndicatorService.getMonthlyBloodPressureByPatientId(patient.getId());
        }

        // 分析血压数据，判断健康风险
        String bloodPressureRisk = "正常";
        if (profile != null) {
            if (profile.getBloodPressure() != null && !profile.getBloodPressure().isEmpty()) {
                String bp = profile.getBloodPressure().toLowerCase();
                if (bp.contains("高") || bp.contains("偏高") || bp.contains("升高")) {
                    bloodPressureRisk = "高血压风险";
                } else if (bp.equals("未记录") || bp.isEmpty()) {
                    bloodPressureRisk = "未测量";
                } else {
                    bloodPressureRisk = "正常";
                }
            } else {
                bloodPressureRisk = "未测量";
            }
        } else {
            bloodPressureRisk = "未测量";
        }

        // 计算BMI风险等级
        String bmiRisk = "正常";
        if (profile != null && profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi < 18.5) {
                bmiRisk = "偏瘦";
            } else if (bmi >= 18.5 && bmi < 24) {
                bmiRisk = "正常";
            } else if (bmi >= 24 && bmi < 28) {
                bmiRisk = "超重";
            } else {
                bmiRisk = "肥胖";
            }
        } else {
            bmiRisk = "未计算";
        }

        // 获取AI健康建议（模拟数据，实际应调用AI服务）
        Map<String, Object> aiSuggestions = generateAISuggestions(profile, patient);

        // 基于AI建议生成个性化饮食建议
        Map<String, Object> personalizedNutrition = generatePersonalizedNutrition(aiSuggestions, profile);

        // 向页面传递数据
        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("profile", profile); // 这里即使是临时的profile也会传递到前端
        model.addAttribute("profileList", profileList);
        model.addAttribute("bloodPressureData", monthlyBloodPressure);
        model.addAttribute("bloodPressureRisk", bloodPressureRisk);
        model.addAttribute("bmiRisk", bmiRisk);
        model.addAttribute("aiSuggestions", aiSuggestions);
        model.addAttribute("personalizedNutrition", personalizedNutrition);

        // 添加调试信息到model
        model.addAttribute("debugInfo", "健康档案状态: " + (profile != null ? "存在" : "不存在"));

        return "patient/nutrition";
    }
    /**
     * 生成AI健康建议（基于患者具体数据的个性化方法）
     */
    private Map<String, Object> generateAISuggestions(HealthProfile profile, Patient patient) {
        Map<String, Object> suggestions = new HashMap<>();

        // 先计算基本风险
        boolean hasHighBloodPressure = false;
        boolean hasOverweight = false;
        boolean hasUnderweight = false;
        boolean hasNormalWeight = false;
        boolean hasNoData = profile == null;

        if (profile != null) {
            // 血压评估
            if (profile.getBloodPressure() != null) {
                String bp = profile.getBloodPressure().toLowerCase();
                if (bp.contains("高") || bp.contains("偏高") || bp.contains("升高") ||
                        (bp.contains("/") && Double.parseDouble(bp.split("/")[0]) > 140)) {
                    hasHighBloodPressure = true;
                }
            }

            // BMI评估
            if (profile.getBmi() != null) {
                double bmi = profile.getBmi();
                if (bmi >= 28) {
                    suggestions.put("bmiCategory", "肥胖");
                    hasOverweight = true;
                } else if (bmi >= 24) {
                    suggestions.put("bmiCategory", "超重");
                    hasOverweight = true;
                } else if (bmi < 18.5) {
                    suggestions.put("bmiCategory", "偏瘦");
                    hasUnderweight = true;
                } else {
                    suggestions.put("bmiCategory", "正常");
                    hasNormalWeight = true;
                }
            } else {
                suggestions.put("bmiCategory", "未计算");
            }
        }

        // 基于患者数据生成建议
        List<String> priorityAreas = new ArrayList<>();
        List<String> specificRecs = new ArrayList<>();

        // 1. 血压相关建议（精确判断）
        if (hasHighBloodPressure) {
            priorityAreas.add("血压管理");
            specificRecs.add("每日食盐摄入量严格控制在5克以下");
            specificRecs.add("增加富含钾的食物摄入，如香蕉、菠菜、土豆");
            specificRecs.add("每周至少测量血压3次，并记录数据");
            specificRecs.add("避免熬夜和情绪激动，保持心情平和");
        } else if (profile != null && profile.getBloodPressure() != null) {
            // 如果血压正常但偏高临界值
            String bp = profile.getBloodPressure();
            if (bp.contains("/")) {
                String[] parts = bp.split("/");
                try {
                    int systolic = Integer.parseInt(parts[0]);
                    int diastolic = Integer.parseInt(parts[1]);
                    if (systolic > 130 || diastolic > 85) {
                        priorityAreas.add("预防高血压");
                        specificRecs.add("注意控制钠摄入，每日食盐不超过6克");
                        specificRecs.add("保持规律运动，每周3-5次中等强度运动");
                    }
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }

        // 2. BMI相关建议（基于具体数值）
        if (hasOverweight) {
            priorityAreas.add("体重管理");
            if (profile != null && profile.getBmi() != null) {
                double bmi = profile.getBmi();
                if (bmi >= 28) {
                    specificRecs.add("建议每日热量摄入减少500-800千卡");
                    specificRecs.add("增加有氧运动，每周5次，每次40-60分钟");
                    specificRecs.add("严格控制碳水化合物摄入，特别是精制糖类");
                } else if (bmi >= 24) {
                    specificRecs.add("建议每日热量摄入减少300-500千卡");
                    specificRecs.add("增加有氧运动，每周4-5次，每次30-40分钟");
                    specificRecs.add("适当减少主食摄入，增加蔬菜比例");
                }
            }
        } else if (hasUnderweight) {
            priorityAreas.add("营养增重");
            if (profile != null && profile.getBmi() != null) {
                double bmi = profile.getBmi();
                if (bmi < 16) {
                    specificRecs.add("增加优质蛋白摄入，每日1.5-2.0g/kg体重");
                    specificRecs.add("适当增加健康脂肪摄入，如坚果、牛油果、橄榄油");
                    specificRecs.add("少量多餐，每日5-6餐，避免一次吃太多");
                } else {
                    specificRecs.add("增加优质蛋白摄入，如鸡蛋、牛奶、鱼虾");
                    specificRecs.add("适当增加主食摄入，选择全谷物");
                    specificRecs.add("保证充足睡眠，促进营养吸收");
                }
            }
        } else if (hasNormalWeight) {
            priorityAreas.add("维持健康体重");
            specificRecs.add("保持当前饮食结构，维持热量平衡");
            specificRecs.add("继续坚持适量运动，保持健康习惯");
        }

        // 3. 身高体重具体建议
        if (profile != null && profile.getHeight() != null && profile.getWeight() != null) {
            double height = profile.getHeight();
            double weight = profile.getWeight();

            // 计算理想体重范围（BMI 18.5-24）
            double minIdealWeight = 18.5 * (height/100) * (height/100);
            double maxIdealWeight = 24 * (height/100) * (height/100);

            if (weight < minIdealWeight) {
                double targetIncrease = minIdealWeight - weight;
                specificRecs.add(String.format("建议增重%.1fkg达到健康体重范围", targetIncrease));
            } else if (weight > maxIdealWeight) {
                double targetDecrease = weight - maxIdealWeight;
                specificRecs.add(String.format("建议减重%.1fkg达到健康体重范围", targetDecrease));
            }
        }

        // 4. 如果没有数据，给通用建议
        if (hasNoData) {
            suggestions.put("overallHealth", "数据不足");
            suggestions.put("priorityAreas", Arrays.asList("完善健康档案", "定期体检", "均衡饮食"));
            suggestions.put("specificRecommendations", Arrays.asList(
                    "请先完善您的健康档案信息",
                    "建议进行全面健康体检",
                    "保持健康生活方式"
            ));
            suggestions.put("hasInsufficientData", true);
            return suggestions;
        }

        // 评估整体健康状况
        String overallHealth = determineOverallHealth(profile);

        // 如果没有特别需要关注的领域，给出维护建议
        if (priorityAreas.isEmpty()) {
            priorityAreas.add("维持现状");
            specificRecs.add("保持均衡饮食，食物多样化");
            specificRecs.add("坚持每周150分钟中等强度运动");
            specificRecs.add("定期健康检查，预防为主");
        }

        suggestions.put("overallHealth", overallHealth);
        suggestions.put("priorityAreas", priorityAreas);
        suggestions.put("specificRecommendations", specificRecs);
        suggestions.put("hasInsufficientData", false);

        return suggestions;
    }

    /**
     * 根据AI建议和患者具体数据生成个性化饮食建议
     */
    private Map<String, Object> generatePersonalizedNutrition(Map<String, Object> aiSuggestions, HealthProfile profile) {
        Map<String, Object> nutrition = new HashMap<>();

        // 按照金字塔顺序：谷薯类（底层）- 蔬菜水果 - 动物性食物 - 奶豆坚果 - 油 - 盐（顶层）
        Map<String, String[]> pyramidData = new LinkedHashMap<>();

        // 默认金字塔数据
        pyramidData.put("谷薯类", new String[]{"每天250-400g", "全谷物占1/3以上", "#FF9E6D"});
        pyramidData.put("蔬菜水果", new String[]{"蔬菜300-500g，水果200-350g", "多样化选择，深色为主", "#9D4EDD"});
        pyramidData.put("动物性食物", new String[]{"每天120-200g", "优选鱼禽，减少红肉", "#118AB2"});
        pyramidData.put("奶豆坚果", new String[]{"奶300g，豆25g，坚果10g", "优选低脂奶制品", "#06D6A0"});
        pyramidData.put("油", new String[]{"每天25-30g", "使用植物油，减少动物油", "#FFD166"});
        pyramidData.put("盐", new String[]{"每天<5g", "低盐饮食，避免腌制食品", "#FF6B6B"});

        // 基于AI建议和患者具体数据调整
        @SuppressWarnings("unchecked")
        List<String> priorities = (List<String>) aiSuggestions.get("priorityAreas");

        // 获取BMI分类
        String bmiCategory = (String) aiSuggestions.get("bmiCategory");

        // 如果有具体患者数据，根据数据调整
        if (profile != null) {
            // 根据BMI调整各层数据
            if ("肥胖".equals(bmiCategory)) {
                pyramidData.put("谷薯类", new String[]{"每天200-250g", "全谷物占2/3以上，严格控制精制谷物", "#FF9E6D"});
                pyramidData.put("油", new String[]{"每天15-20g", "严格控制油脂，避免油炸食品", "#FFD166"});
                pyramidData.put("动物性食物", new String[]{"每天100-150g", "优选低脂肉类，以鱼虾为主", "#118AB2"});
            } else if ("超重".equals(bmiCategory)) {
                pyramidData.put("谷薯类", new String[]{"每天250-300g", "全谷物占1/2以上，减少精制谷物", "#FF9E6D"});
                pyramidData.put("油", new String[]{"每天20-25g", "控制油脂摄入，选择健康油脂", "#FFD166"});
            } else if ("偏瘦".equals(bmiCategory)) {
                pyramidData.put("谷薯类", new String[]{"每天300-400g", "可适当增加全谷物和薯类", "#FF9E6D"});
                pyramidData.put("动物性食物", new String[]{"每天150-200g", "增加优质蛋白摄入", "#118AB2"});
                pyramidData.put("奶豆坚果", new String[]{"奶400g，豆30g，坚果15g", "适当增加摄入量", "#06D6A0"});
            }

            // 根据血压情况调整
            if (profile.getBloodPressure() != null &&
                    profile.getBloodPressure().toLowerCase().contains("高")) {
                pyramidData.put("盐", new String[]{"每天<3g", "严格低盐，避免酱油、味精等高钠调料", "#FF6B6B"});
                pyramidData.put("蔬菜水果", new String[]{"蔬菜500g，水果300g", "增加高钾蔬菜如菠菜、土豆、蘑菇", "#9D4EDD"});
                pyramidData.put("动物性食物", new String[]{"每天100-150g", "减少红肉，以鱼虾、禽肉为主", "#118AB2"});
            }

            // 如果有身高体重数据，计算更精确的建议
            if (profile.getHeight() != null && profile.getWeight() != null) {
                double height = profile.getHeight();
                double weight = profile.getWeight();

                // 根据身高调整各层推荐量
                if (height < 160) { // 身高较矮
                    pyramidData.put("谷薯类", new String[]{String.format("每天200-300g", height), "全谷物占1/3以上", "#FF9E6D"});
                    pyramidData.put("动物性食物", new String[]{String.format("每天100-150g", weight), "优选鱼禽，减少红肉", "#118AB2"});
                } else if (height > 175) { // 身高较高
                    pyramidData.put("谷薯类", new String[]{String.format("每天300-450g", height), "全谷物占1/3以上", "#FF9E6D"});
                    pyramidData.put("动物性食物", new String[]{String.format("每天150-220g", weight), "优选鱼禽，减少红肉", "#118AB2"});
                    pyramidData.put("奶豆坚果", new String[]{"奶350g，豆30g，坚果15g", "优选低脂奶制品", "#06D6A0"});
                }
            }
        }

        // 饮水建议 - 根据体重和活动量调整
        String waterAdvice = "每天1500-1700ml";
        if (profile != null && profile.getWeight() != null) {
            double weight = profile.getWeight();
            // 按每公斤体重30-40ml计算
            int waterML = (int)(weight * 35);
            waterAdvice = String.format("每天%d-%dml", waterML - 200, waterML + 200);

            if (priorities.contains("体重管理")) {
                waterAdvice += "（饭前饮水增加饱腹感）";
            }
        }

        nutrition.put("pyramidData", pyramidData);
        nutrition.put("waterAdvice", waterAdvice);
        nutrition.put("mealPlan", generateMealPlan(priorities, profile, aiSuggestions));
        nutrition.put("foodsToAvoid", generateFoodsToAvoid(priorities, profile));

        // 添加患者特有的提示
        if (profile != null) {
            List<String> personalizedTips = new ArrayList<>();

            if (profile.getHeight() != null && profile.getWeight() != null) {
                personalizedTips.add(String.format("根据您的身高%.1fcm和体重%.1fkg，建议控制总热量摄入",
                        profile.getHeight(), profile.getWeight()));
            }

            if (profile.getBloodPressure() != null &&
                    !profile.getBloodPressure().toLowerCase().contains("未")) {
                personalizedTips.add("基于您的血压数据" + profile.getBloodPressure() + "，建议低钠饮食");
            }

            if (bmiCategory != null && !bmiCategory.equals("正常")) {
                personalizedTips.add("您的BMI分类为：" + bmiCategory + "，需要特别关注相关饮食建议");
            }

            nutrition.put("personalizedTips", personalizedTips);
        }

        return nutrition;
    }

    /**
     * 生成个性化餐单（基于患者数据）
     */
    private Map<String, String> generateMealPlan(List<String> priorities, HealthProfile profile, Map<String, Object> aiSuggestions) {
        Map<String, String> mealPlan = new LinkedHashMap<>();

        String bmiCategory = (String) aiSuggestions.get("bmiCategory");
        boolean hasHighBP = priorities.contains("血压管理");
        boolean isOverweight = "超重".equals(bmiCategory) || "肥胖".equals(bmiCategory);
        boolean isUnderweight = "偏瘦".equals(bmiCategory);

        // 根据患者的具体情况生成不同的餐单
        if (hasHighBP && isOverweight) {
            // 高血压+超重/肥胖
            mealPlan.put("早餐", "燕麦粥50g + 水煮蛋1个 + 低脂牛奶200ml + 凉拌菠菜100g（不加盐）");
            mealPlan.put("午餐", "糙米饭80g + 清蒸鱼150g + 凉拌西兰花200g + 紫菜汤（少盐）");
            mealPlan.put("晚餐", "蒸红薯150g + 豆腐羹100g + 白灼生菜200g + 蒸山药50g");
            mealPlan.put("加餐", "苹果1个 + 核桃2个");
            mealPlan.put("特别提示", "严格控盐，全天食盐<3g；控制总热量，晚餐宜早不宜晚");

        } else if (hasHighBP && !isOverweight) {
            // 高血压但体重正常或偏瘦
            mealPlan.put("早餐", "全麦面包2片 + 鸡蛋1个 + 无糖豆浆200ml + 凉拌黄瓜100g");
            mealPlan.put("午餐", "杂粮饭100g + 清蒸鸡胸肉150g + 炒菠菜200g + 冬瓜汤（少盐）");
            mealPlan.put("晚餐", "蒸南瓜150g + 虾仁炒西芹150g + 凉拌海带丝100g");
            mealPlan.put("加餐", "香蕉1根 + 杏仁10颗");
            mealPlan.put("特别提示", "控盐同时保证营养，增加高钾食物摄入");

        } else if (isOverweight && !hasHighBP) {
            // 超重/肥胖但血压正常
            mealPlan.put("早餐", "全麦馒头1个 + 鸡蛋1个 + 脱脂牛奶200ml + 圣女果100g");
            mealPlan.put("午餐", "杂粮饭80g + 鸡胸肉150g + 凉拌木耳黄瓜200g + 番茄蛋花汤");
            mealPlan.put("晚餐", "蒸玉米1根 + 蒸鱼150g + 凉拌西兰花200g");
            mealPlan.put("加餐", "希腊酸奶100g + 蓝莓50g");
            mealPlan.put("特别提示", "控制总热量，增加膳食纤维，保证蛋白质摄入");

        } else if (isUnderweight) {
            // 偏瘦需要增重
            mealPlan.put("早餐", "燕麦粥60g + 鸡蛋2个 + 全脂牛奶200ml + 牛油果50g");
            mealPlan.put("午餐", "米饭150g + 红烧鸡腿200g + 炒青菜200g + 豆腐汤");
            mealPlan.put("晚餐", "杂粮粥200g + 牛肉炒蔬菜200g + 蒸蛋羹");
            mealPlan.put("加餐", "坚果30g + 香蕉1根 + 酸奶150g");
            mealPlan.put("特别提示", "增加热量和蛋白质摄入，可少量多餐");

        } else {
            // 正常健康状态
            mealPlan.put("早餐", "全麦面包2片 + 鸡蛋1个 + 牛奶200ml + 水果150g");
            mealPlan.put("午餐", "米饭120g + 瘦肉150g + 蔬菜250g + 汤");
            mealPlan.put("晚餐", "杂粮粥150g + 鱼虾150g + 蔬菜200g");
            mealPlan.put("加餐", "酸奶100g + 坚果20g");
            mealPlan.put("特别提示", "保持均衡饮食，食物多样化");
        }

        // 如果有具体身高体重，添加更多个性化建议
        if (profile != null && profile.getHeight() != null && profile.getWeight() != null) {
            double bmi = profile.getBmi() != null ? profile.getBmi() :
                    profile.getWeight() / ((profile.getHeight()/100) * (profile.getHeight()/100));

            if (bmi >= 28) {
                mealPlan.put("热量控制", "建议每日热量控制在1200-1500千卡");
            } else if (bmi >= 24) {
                mealPlan.put("热量控制", "建议每日热量控制在1500-1800千卡");
            } else if (bmi < 18.5) {
                mealPlan.put("热量控制", "建议每日热量增加到2000-2200千卡");
            }
        }

        return mealPlan;
    }


    /**
     * 生成禁忌食物列表（基于患者具体情况）
     */
    private List<String> generateFoodsToAvoid(List<String> priorities, HealthProfile profile) {
        List<String> foodsToAvoid = new ArrayList<>();

        // 通用禁忌
        foodsToAvoid.add("油炸食品：炸鸡、薯条、油条等");
        foodsToAvoid.add("加工肉类：火腿、香肠、培根等");
        foodsToAvoid.add("含糖饮料：可乐、果汁、奶茶等");

        // 基于AI建议的禁忌
        if (priorities.contains("血压管理")) {
            foodsToAvoid.add("高盐食品：咸菜、腊肉、酱油制品、方便面");
            foodsToAvoid.add("腌制食品：泡菜、咸鱼、酱料、腐乳");
            foodsToAvoid.add("高钠零食：薯片、话梅、海苔");
            foodsToAvoid.add("浓汤和火锅汤底：含钠量极高");
        }

        if (priorities.contains("体重管理")) {
            foodsToAvoid.add("高热量零食：蛋糕、饼干、巧克力、冰淇淋");
            foodsToAvoid.add("精细主食：白面包、白米饭、面条、馒头");
            foodsToAvoid.add("高脂肪肉类：五花肉、肥牛、动物内脏");
            foodsToAvoid.add("含糖量高的水果：荔枝、龙眼、芒果、榴莲");
        }

        // 基于BMI的额外禁忌
        if (profile != null && profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi >= 28) { // 肥胖
                foodsToAvoid.add("所有含糖饮料和甜点");
                foodsToAvoid.add("油炸食品和烧烤类");
                foodsToAvoid.add("精制谷物和淀粉类食物");
            } else if (bmi >= 24) { // 超重
                foodsToAvoid.add("高糖水果和果汁");
                foodsToAvoid.add("肥肉和动物皮");
                foodsToAvoid.add("含糖调味品：番茄酱、沙拉酱");
            } else if (bmi < 18.5) { // 偏瘦
                foodsToAvoid.add("避免只吃蔬菜水果忽略主食和蛋白质");
                foodsToAvoid.add("减少影响食欲的刺激性食物");
            }
        }

        // 如果有血压数据，添加更多针对性禁忌
        if (profile != null && profile.getBloodPressure() != null) {
            String bp = profile.getBloodPressure().toLowerCase();
            if (bp.contains("高") || bp.contains("偏高")) {
                foodsToAvoid.add("酒类：特别是白酒、啤酒");
                foodsToAvoid.add("浓茶和咖啡：可能影响血压稳定");
                foodsToAvoid.add("动物内脏和脑花：高胆固醇");
            }
        }

        return foodsToAvoid;
    }

    /**
     * 评估整体健康状况
     */
    private String determineOverallHealth(HealthProfile profile) {
        int score = 0;

        if (profile.getBloodPressure() != null &&
                !profile.getBloodPressure().toLowerCase().contains("高")) {
            score++;
        }

        if (profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi >= 18.5 && bmi < 24) {
                score++;
            }
        }

        if (score >= 2) return "良好";
        if (score == 1) return "一般";
        return "需改善";
    }




    @GetMapping("/health-profile")
    public String healthProfileList(HttpSession session, Model model) {
        final String logPrefix = "健康档案列表请求处理 - ";
        log.info("{}开始", logPrefix);

        try {
            // 认证
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                log.warn("{}用户未认证", logPrefix);
                return "redirect:/login";
            }

            String username = auth.getName();
            log.info("{}认证用户: {}", logPrefix, username);

            // 获取用户
            User user = userService.findByUsername(username);
            if (user == null) {
                log.error("{}找不到用户: {}", logPrefix, username);
                model.addAttribute("errorMessage", "用户不存在");
                return "redirect:/login";
            }

            log.info("{}找到用户: {}", logPrefix, user.getUsername());

            if (!"PATIENT".equals(user.getUserType())) {
                log.error("{}用户不是患者类型: {}", logPrefix, user.getUserType());
                model.addAttribute("errorMessage", "仅限患者访问");
                return "redirect:/login";
            }

            // 获取患者
            Patient patient = getOrCreatePatient(user, session);
            if (patient == null || patient.getId() == null) {
                log.error("{}患者信息不完整", logPrefix);
                model.addAttribute("errorMessage", "患者信息不完整，请联系管理员");
                // 仍然设置默认值，让页面可以显示
                setDefaultStatistics(model);
                return "patient/health-profile";
            }

            log.info("{}患者信息: id={}", logPrefix, patient.getId());

            // 查询健康档案
            List<HealthProfile> profileList;
            try {
                log.info("{}正在查询患者ID为 {} 的健康档案", logPrefix, patient.getId());
                profileList = healthProfileService.getByPatientId(patient.getId());
                log.info("{}成功查询到 {} 条健康档案记录", logPrefix, profileList.size());

                // 添加到模型
                model.addAttribute("profileList", profileList);

            } catch (Exception e) {
                log.error("{}查询健康档案时发生异常: {}", logPrefix, e.getMessage(), e);
                model.addAttribute("errorMessage", "获取健康档案失败，请稍后重试");
                profileList = Collections.emptyList();
                model.addAttribute("profileList", profileList);
            }

            // ============ 关键部分：计算UI所需的所有统计数据 ============
            calculateAndAddStatistics(model, profileList, patient.getId());
            // ========================================================

            // 添加用户和患者信息到模型
            model.addAttribute("user", user);
            model.addAttribute("patient", patient);

            log.info("{}处理完成", logPrefix);
            return "patient/health-profile";

        } catch (Exception e) {
            log.error("{}处理过程中发生未预期异常: {}", logPrefix, e.getMessage(), e);
            model.addAttribute("errorMessage", "系统异常，请联系管理员");
            setDefaultStatistics(model);
            return "patient/health-profile";
        }
    }

    /**
     * 计算并添加UI所需的所有统计数据
     */
    private void calculateAndAddStatistics(Model model, List<HealthProfile> profileList, Long patientId) {
        // 1. 健康档案总数
        int totalProfiles = profileList.size();
        model.addAttribute("totalProfiles", totalProfiles);

        // 2. 计算较上月增长百分比
        // 注意：这里需要上个月的数据，你可能需要调整这个方法
        String growthPercentage = calculateGrowthPercentage(patientId, totalProfiles);
        model.addAttribute("growthPercentage", growthPercentage);

        // 3. 健康状态良好的档案数量
        long goodHealthCount = profileList.stream()
                .filter(this::isGoodHealth)
                .count();
        model.addAttribute("goodHealthCount", goodHealthCount);

        // 4. 需要关注指标的档案数量
        long needAttentionCount = profileList.stream()
                .filter(this::needsAttention)
                .count();
        model.addAttribute("needAttentionCount", needAttentionCount);

        // 5. 如果有数据，还可以计算其他信息
        if (!profileList.isEmpty()) {
            // 获取最新的档案记录
            HealthProfile latestProfile = profileList.get(profileList.size() - 1);
            model.addAttribute("latestProfile", latestProfile);

            // 计算平均BMI
            double avgBmi = profileList.stream()
                    .filter(p -> p.getBmi() != null)
                    .mapToDouble(HealthProfile::getBmi)
                    .average()
                    .orElse(0.0);
            model.addAttribute("avgBmi", String.format("%.1f", avgBmi));
        }
    }

    /**
     * 计算较上月的增长百分比
     * 你需要根据实际业务逻辑调整这个方法
     */
    private String calculateGrowthPercentage(Long patientId, int currentMonthCount) {
        try {
            // 假设你有方法获取上个月的记录数
            // int lastMonthCount = healthProfileService.getLastMonthCount(patientId);
            // 如果上个月为0，增长率就是100%
            // if (lastMonthCount == 0) {
            //     return currentMonthCount > 0 ? "100%" : "0%";
            // }
            // double growth = ((double)(currentMonthCount - lastMonthCount) / lastMonthCount) * 100;
            // return String.format("%.0f%%", growth);

            // 临时返回固定值，根据你的UI显示
            return "12%";
        } catch (Exception e) {
            log.error("计算增长百分比失败: {}", e.getMessage());
            return "0%";
        }
    }

    /**
     * 判断健康状态是否良好
     */
    private boolean isGoodHealth(HealthProfile profile) {
        // 根据你的业务逻辑判断
        // 示例：BMI在18.5-24之间，血压正常
        boolean isGood = true;

        // 检查BMI
        if (profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi < 18.5 || bmi > 24) {
                isGood = false;
            }
        }

        // 检查血压（如果有）
        if (profile.getBloodPressure() != null) {
            String bp = profile.getBloodPressure();
            // 简单的血压判断逻辑，需要根据实际情况调整
            if (bp.contains("/")) {
                String[] parts = bp.split("/");
                if (parts.length == 2) {
                    try {
                        int systolic = Integer.parseInt(parts[0].trim());
                        int diastolic = Integer.parseInt(parts[1].trim());

                        // 正常血压范围：收缩压90-140，舒张压60-90
                        if (systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90) {
                            isGood = false;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("血压格式错误: {}", bp);
                    }
                }
            }
        }

        return isGood;
    }

    /**
     * 判断是否需要关注
     */
    private boolean needsAttention(HealthProfile profile) {
        // 如果有任何一项指标异常，就需要关注
        return !isGoodHealth(profile);

        // 或者更精确的判断逻辑：
        // boolean needsAttention = false;
        //
        // // BMI异常
        // if (profile.getBmi() != null && (profile.getBmi() < 18.5 || profile.getBmi() > 28)) {
        //     needsAttention = true;
        // }
        //
        // // 血压异常
        // if (profile.getBloodPressure() != null) {
        //     // 解析血压逻辑
        // }
        //
        // return needsAttention;
    }

    /**
     * 设置默认的统计数据（当没有数据或出错时）
     */
    private void setDefaultStatistics(Model model) {
        model.addAttribute("totalProfiles", 0);
        model.addAttribute("growthPercentage", "0%");
        model.addAttribute("goodHealthCount", 0);
        model.addAttribute("needAttentionCount", 0);
        model.addAttribute("profileList", Collections.emptyList());
    }
    /**
     * 获取健康档案详情（用于弹窗显示）
     */
    @GetMapping("/health-profile/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHealthProfileDetail(
            @PathVariable Long id,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 从Spring Security上下文中获取认证用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("msg", "未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 获取用户信息
            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null) {
                user = userService.findByEmployeeId(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("msg", "用户不存在或不是患者");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            Patient patient = (Patient) session.getAttribute("patient");
            if (patient == null) {
                patient = patientService.getByUserId(user.getId());
                if (patient != null) {
                    session.setAttribute("patient", patient);
                }
            }

            if (patient == null) {
                result.put("sucess", false);
                result.put("msg", "患者信息不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 获取健康档案
            HealthProfile profile = healthProfileService.getById(id);
            if (profile == null) {
                result.put("success", false);
                result.put("msg", "健康档案不存在");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证是否是当前患者的档案
            if (profile.getPatientId() == null || !profile.getPatientId().equals(patient.getId())) {
                result.put("success", false);
                result.put("msg", "无权查看该健康档案");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            // 构建返回数据 - 确保包含所有字段
            Map<String, Object> profileDetail = new HashMap<>();

            // 基本信息和ID
            profileDetail.put("id", profile.getId());

            // 基本体征信息
            profileDetail.put("height", profile.getHeight() != null ? String.format("%.1f cm", profile.getHeight()) : "未记录");
            profileDetail.put("weight", profile.getWeight() != null ? String.format("%.1f kg", profile.getWeight()) : "未记录");
            profileDetail.put("bmi", profile.getBmi() != null ? String.format("%.2f", profile.getBmi()) : "未计算");

            // 血压和心率
            profileDetail.put("bloodPressure",
                    profile.getBloodPressure() != null && !profile.getBloodPressure().trim().isEmpty() ?
                            profile.getBloodPressure() : "未记录");
            profileDetail.put("heartRate",
                    profile.getHeartRate() != null ?
                            profile.getHeartRate() + " 次/分" : "未记录");

            // 血型
            profileDetail.put("bloodType",
                    profile.getBloodType() != null && !profile.getBloodType().trim().isEmpty() ?
                            profile.getBloodType() + " 型" : "未记录");

            // 检查日期
            profileDetail.put("lastPhysicalDate",
                    profile.getLastPhysicalDate() != null ?
                            profile.getLastPhysicalDate().toString() : "未记录");

//            // 病史信息
//            profileDetail.put("familyMedicalHistory",
//                    profile.getFamilyMedicalHistory() != null && !profile.getFamilyMedicalHistory().trim().isEmpty() ?
//                            profile.getFamilyMedicalHistory() : "无");
//            profileDetail.put("medicalHistory",
//                    profile.getMedicalHistory() != null && !profile.getMedicalHistory().trim().isEmpty() ?
//                            profile.getMedicalHistory() : "无");
//            profileDetail.put("allergies",
//                    profile.getAllergies() != null && !profile.getAllergies().trim().isEmpty() ?
//                            profile.getAllergies() : "无");

            // 病史信息
            profileDetail.put("familyMedicalHistory",
                    profile.getFamilyMedicalHistory() != null && !profile.getFamilyMedicalHistory().trim().isEmpty() ?
                            profile.getFamilyMedicalHistory() : "无");

            // 生活习惯
            profileDetail.put("lifestyleHabits",
                    profile.getLifestyleHabits() != null && !profile.getLifestyleHabits().trim().isEmpty() ?
                            profile.getLifestyleHabits() : "无记录");

            // 健康评估信息
            profileDetail.put("healthStatus", evaluateHealthStatus(profile));
            profileDetail.put("healthTips", generateHealthTips(profile));

            result.put("success", true);
            result.put("profile", profileDetail);

            log.info("返回健康档案详情，档案ID: {}", id);

        } catch (Exception e) {
            log.error("获取健康档案详情失败", e);
            result.put("success", false);
            result.put("msg", "系统错误：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }


    /**
     * 健康状态评估
     */
    private String evaluateHealthStatus(HealthProfile profile) {
        if (profile == null) return "未评估";

        StringBuilder status = new StringBuilder();

        // 血压评估
        if (profile.getBloodPressure() != null) {
            try {
                String bp = profile.getBloodPressure();
                if (bp.contains("/")) {
                    String[] parts = bp.split("/");
                    if (parts.length >= 2) {
                        int systolic = Integer.parseInt(parts[0]);
                        int diastolic = Integer.parseInt(parts[1]);

                        if (systolic >= 140 || diastolic >= 90) {
                            status.append("血压偏高 ");
                        } else if (systolic >= 130 || diastolic >= 85) {
                            status.append("血压正常偏高 ");
                        } else {
                            status.append("血压正常 ");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        // BMI评估
        if (profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi < 18.5) {
                status.append("体重偏轻 ");
            } else if (bmi < 24) {
                status.append("体重正常 ");
            } else if (bmi < 28) {
                status.append("体重超重 ");
            } else {
                status.append("肥胖 ");
            }
        }

        // 心率评估
        if (profile.getHeartRate() != null) {
            int hr = profile.getHeartRate();
            if (hr < 60) {
                status.append("心率偏慢 ");
            } else if (hr > 100) {
                status.append("心率偏快 ");
            } else {
                status.append("心率正常 ");
            }
        }

        return status.toString().trim();
    }

    /**
     * 生成健康建议
     */
    private List<String> generateHealthTips(HealthProfile profile) {
        List<String> tips = new ArrayList<>();

        if (profile == null) {
            tips.add("请完善您的健康档案信息");
            return tips;
        }

        // 血压建议
        if (profile.getBloodPressure() != null) {
            try {
                String bp = profile.getBloodPressure();
                if (bp.contains("/")) {
                    String[] parts = bp.split("/");
                    int systolic = Integer.parseInt(parts[0]);
                    int diastolic = Integer.parseInt(parts[1]);

                    if (systolic >= 140 || diastolic >= 90) {
                        tips.add("建议定期监测血压，减少钠盐摄入");
                        tips.add("保持规律运动，控制情绪波动");
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        // BMI建议
        if (profile.getBmi() != null) {
            double bmi = profile.getBmi();
            if (bmi < 18.5) {
                tips.add("建议增加营养摄入，适当进行力量训练");
            } else if (bmi >= 24) {
                tips.add("建议控制饮食热量，增加有氧运动");
            }
        }

        // 心率建议
        if (profile.getHeartRate() != null) {
            int hr = profile.getHeartRate();
            if (hr > 100) {
                tips.add("建议减少咖啡因摄入，避免过度劳累");
            } else if (hr < 60 && profile.getHeartRate() != null) {
                tips.add("如无症状可继续观察，如有不适请咨询医生");
            }
        }

        // 通用建议
        if (tips.isEmpty()) {
            tips.add("继续保持健康生活方式");
            tips.add("均衡饮食，适量运动");
            tips.add("定期体检，预防为主");
        }

        return tips;
    }




//xmk
    // 在 PatientController.java 中添加以下方法（可以放在文件末尾）

    /**
     * 电子病历查看页面
     */
    // ... existing code ...

    /**
     * 电子病历查看页面
     */
    // ... existing code ...

    /**
     * 电子病历查看页面
     */
//    @GetMapping("/medical-records")
//    public String viewMedicalRecords(HttpSession session, Model model) {
//        // 从Spring Security上下文中获取认证用户信息
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
//            return "redirect:/login";
//        }
//
//        // 获取当前登录的用户信息
//        User user = userService.findByUsername(auth.getName());
//        if (user == null) {
//            user = userService.findByPhone(auth.getName());
//        }
//        if (user == null) {
//            user = userService.findByEmployeeId(auth.getName());
//        }
//        if (user == null || !"PATIENT".equals(user.getUserType())) {
//            return "redirect:/login";
//        }
//
//        System.out.println("当前登录用户ID: " + user.getId() + ", 用户名: " + user.getUsername() + ", 类型: " + user.getUserType());
//
//        // 获取患者的病历记录
//        Patient patient = patientService.getByUserId(user.getId());
//        System.out.println("根据用户ID " + user.getId() + " 查询到的患者: " + patient);
//
//        if (patient != null) {
//            System.out.println("找到患者ID: " + patient.getId());
//
//            // 先检查是否有病历记录
//            List<MedicalRecord> allRecords = medicalRecordService.list();
//            System.out.println("数据库中总共有 " + allRecords.size() + " 条病历记录");
//
//            for (MedicalRecord record : allRecords) {
//                System.out.println("病历ID: " + record.getId() + ", 患者ID: " + record.getPatientId() + ", 诊断: " + record.getDiagnosis());
//            }
//
//            List<MedicalRecord> medicalRecords = medicalRecordService.listByPatientId(patient.getId());
//            System.out.println("根据患者ID " + patient.getId() + " 查询到 " + medicalRecords.size() + " 条病历记录");
//
//            // 创建包含医生和科室信息的记录列表
//            List<Map<String, Object>> recordMaps = new ArrayList<>();
//            for (MedicalRecord record : medicalRecords) {
//                Map<String, Object> recordMap = new HashMap<>();
//                recordMap.put("record", record);
//
//                // 获取医生信息
//                String doctorName = "未知医生";
//                String departmentName = "未知科室";
//
//                if (record.getDoctorId() != null) {
//                    Doctor doctor = doctorService.getById(record.getDoctorId());
//                    if (doctor != null) {
//                        User doctorUser = userService.getById(doctor.getUserId());
//                        if (doctorUser != null) {
//                            doctorName = doctorUser.getRealName() != null ? doctorUser.getRealName() : "未知医生";
//                        }
//
//                        if (doctor.getDepartmentId() != null) {
//                            Department department = departmentService.getById(doctor.getDepartmentId());
//                            if (department != null) {
//                                departmentName = department.getName();
//                            }
//                        }
//                    }
//                }
//
//                recordMap.put("doctorName", doctorName);
//                recordMap.put("departmentName", departmentName);
//                recordMaps.add(recordMap);
//            }
//
//            model.addAttribute("recordMaps", recordMaps);
//            model.addAttribute("medicalRecords", medicalRecords);
//            System.out.println("传递到前端的recordMaps数量: " + recordMaps.size());
//        } else {
//            System.out.println("未找到关联的患者记录，用户ID: " + user.getId());
//            model.addAttribute("recordMaps", new ArrayList<>());
//            model.addAttribute("medicalRecords", new ArrayList<>());
//        }
//
//        model.addAttribute("user", user);
//        return "patient/medical-records";
//    }

// ... existing code ...
// ... existing code ...





    /**
     * 电子病历查看页面
     */
    @GetMapping("/medical-records")
    public String medicalRecords(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null || !"PATIENT".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Patient patient = getOrCreatePatient(user, session);

        List<MedicalRecord> medicalRecords = new ArrayList<>();
        if (patient.getId() != null) {
            medicalRecords = medicalRecordService.getByPatientId(patient.getId());
        }

        List<Map<String, Object>> recordList = new ArrayList<>();
        for (MedicalRecord record : medicalRecords) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("visitDate", record.getVisitDate());
            recordMap.put("diagnosis", record.getDiagnosis());

            // 处理治疗方案
            String treatmentPlan = record.getTreatmentPlan();
            if (treatmentPlan != null && !treatmentPlan.trim().isEmpty()) {
                try {
                    if (treatmentPlan.trim().startsWith("[")) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<Map<String, Object>> medications = mapper.readValue(treatmentPlan, List.class);

                        StringBuilder formattedPlan = new StringBuilder();
                        for (Map<String, Object> med : medications) {
                            String medicineName = (String) med.get("medicineName");
                            String genericName = (String) med.get("genericName");
                            String specification = (String) med.get("specification");
                            String frequency = (String) med.get("frequency");
                            Integer days = med.get("days") != null ? (Integer) med.get("days") : null;

                            String displayName = medicineName != null ? medicineName :
                                    (genericName != null ? genericName : "未知药品");

                            formattedPlan.append(displayName);
                            if (specification != null && !specification.isEmpty()) {
                                formattedPlan.append(" (").append(specification).append(")");
                            }
                            if (frequency != null && !frequency.isEmpty()) {
                                formattedPlan.append("，").append(frequency);
                            }
                            if (days != null && days > 0) {
                                formattedPlan.append("，共").append(days).append("天");
                            }
                            formattedPlan.append("； ");
                        }

                        if (formattedPlan.length() > 0) {
                            String result = formattedPlan.toString();
                            if (result.endsWith("； ")) {
                                result = result.substring(0, result.length() - 2);
                            }
                            recordMap.put("treatmentPlan", result);
                        } else {
                            recordMap.put("treatmentPlan", treatmentPlan);
                        }
                    } else {
                        recordMap.put("treatmentPlan", treatmentPlan);
                    }
                } catch (Exception e) {
                    recordMap.put("treatmentPlan", treatmentPlan);
                }
            } else {
                recordMap.put("treatmentPlan", "暂无数据");
            }

            // 获取费用 - 直接调用 medicalRecordService.calculateTotalPrice
            try {
                com.example.keshe1.dto.MedicalRecordPriceDTO priceDTO = medicalRecordService.calculateTotalPrice(record.getId());
                if (priceDTO != null && priceDTO.getTotalPrice() != null) {
                    recordMap.put("totalPrice", "¥" + priceDTO.getTotalPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
                    recordMap.put("totalPriceValue", priceDTO.getTotalPrice());
                } else {
                    recordMap.put("totalPrice", "¥0.00");
                    recordMap.put("totalPriceValue", BigDecimal.ZERO);
                }
            } catch (Exception e) {
                recordMap.put("totalPrice", "¥0.00");
                recordMap.put("totalPriceValue", BigDecimal.ZERO);
            }

            // 获取医生和科室信息
            if (record.getDoctorId() != null) {
                Doctor doctor = doctorService.getById(record.getDoctorId());
                if (doctor != null) {
                    User doctorUser = userService.getById(doctor.getUserId());
                    if (doctorUser != null) {
                        recordMap.put("doctorName", doctorUser.getRealName());
                    }
                    if (doctor.getDepartmentId() != null) {
                        Department department = departmentService.getById(doctor.getDepartmentId());
                        if (department != null) {
                            recordMap.put("departmentName", department.getName());
                        }
                    }
                }
            }

            recordList.add(recordMap);
        }

        model.addAttribute("user", user);
        model.addAttribute("patient", patient);
        model.addAttribute("medicalRecords", recordList);
        return "patient/medical-records";
    }

    /**
     * 内部方法：调用医生端的费用计算逻辑
     */
    private Map<String, Object> getMedicalRecordPriceFromDoctorService(Long recordId) {
        Map<String, Object> result = new HashMap<>();

        try {
            MedicalRecord record = medicalRecordService.getById(recordId);
            if (record == null) {
                return result;
            }

            // 1. 挂号费（从医生表获取）
            BigDecimal registrationFee = new BigDecimal("0.00");
            if (record.getDoctorId() != null) {
                Doctor doctor = doctorService.getById(record.getDoctorId());
                if (doctor != null && doctor.getRegistrationFee() != null) {
                    registrationFee = doctor.getRegistrationFee();
                } else {
                    registrationFee = new BigDecimal("10.00");
                }
            }

            // 2. 药费（从treatmentPlan解析）
            BigDecimal medicineFee = BigDecimal.ZERO;
            String treatmentPlan = record.getTreatmentPlan();
            if (treatmentPlan != null && treatmentPlan.trim().startsWith("[")) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    List<Map<String, Object>> meds = mapper.readValue(treatmentPlan, List.class);
                    for (Map<String, Object> med : meds) {
                        Object priceObj = med.get("price");
                        if (priceObj != null) {
                            BigDecimal price = new BigDecimal(priceObj.toString());
                            int quantity = 1;
                            if (med.get("quantity") != null) {
                                quantity = (Integer) med.get("quantity");
                            } else if (med.get("totalQuantity") != null) {
                                quantity = (Integer) med.get("totalQuantity");
                            }
                            medicineFee = medicineFee.add(price.multiply(new BigDecimal(quantity)));
                        }
                    }
                } catch (Exception e) {
                    medicineFee = BigDecimal.ZERO;
                }
            }

            // 3. 检查费（从examination表关联examination_item表）
            BigDecimal examinationFee = BigDecimal.ZERO;
            try {
                List<Examination> examinations = examinationService.getByRecordId(record.getId());
                if (examinations != null && !examinations.isEmpty()) {
                    for (Examination exam : examinations) {
                        if (exam.getExaminationItemId() != null) {
                            ExaminationItem item = examinationItemService.getById(exam.getExaminationItemId());
                            if (item != null && item.getPrice() != null) {
                                examinationFee = examinationFee.add(item.getPrice());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                examinationFee = BigDecimal.ZERO;
            }

            BigDecimal totalPrice = registrationFee.add(medicineFee).add(examinationFee);

            result.put("registrationFee", registrationFee);
            result.put("medicineFee", medicineFee);
            result.put("examinationFee", examinationFee);
            result.put("totalPrice", totalPrice);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



    /**
     * 获取电子病历详情（用于弹窗显示）
     */
    @GetMapping("/medical-records/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMedicalRecordDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("msg", "未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 获取当前登录用户
            String username = auth.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                user = userService.findByPhone(username);
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("msg", "用户不存在或不是患者");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 获取患者信息 - 关键修复：直接从数据库查询，不依赖session
            Patient patient = patientService.getByUserId(user.getId());
            if (patient == null) {
                result.put("success", false);
                result.put("msg", "患者信息不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            // 获取病历
            MedicalRecord record = medicalRecordService.getById(id);
            if (record == null) {
                result.put("success", false);
                result.put("msg", "电子病历记录不存在");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证权限：病历必须属于当前患者
            if (!record.getPatientId().equals(patient.getId())) {
                result.put("success", false);
                result.put("msg", "无权查看该电子病历");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            // 构建返回数据
            Map<String, Object> recordDetail = new HashMap<>();
            recordDetail.put("id", record.getId());
            recordDetail.put("visitDate", record.getVisitDate() != null ? record.getVisitDate().toString() : "");
            recordDetail.put("chiefComplaint", record.getChiefComplaint() != null ? record.getChiefComplaint() : "暂无数据");
            recordDetail.put("presentIllness", record.getPresentIllness() != null ? record.getPresentIllness() : "暂无数据");
            recordDetail.put("pastIllness", record.getPastIllness() != null ? record.getPastIllness() : "暂无数据");
            recordDetail.put("physicalExamination", record.getPhysicalExamination() != null ? record.getPhysicalExamination() : "暂无数据");
            recordDetail.put("auxiliaryExamination", record.getAuxiliaryExamination() != null ? record.getAuxiliaryExamination() : "暂无数据");
            recordDetail.put("diagnosis", record.getDiagnosis() != null ? record.getDiagnosis() : "暂无数据");

            // 处理治疗方案
            String treatmentPlan = record.getTreatmentPlan();
            if (treatmentPlan != null && !treatmentPlan.trim().isEmpty()) {
                try {
                    if (treatmentPlan.trim().startsWith("[")) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<Map<String, Object>> medications = mapper.readValue(treatmentPlan, List.class);
                        StringBuilder formattedPlan = new StringBuilder();
                        for (Map<String, Object> med : medications) {
                            String medicineName = (String) med.get("medicineName");
                            String genericName = (String) med.get("genericName");
                            String specification = (String) med.get("specification");
                            String dosageForm = (String) med.get("dosageForm");
                            String dosageUnit = (String) med.get("dosageUnit");
                            String frequency = (String) med.get("frequency");
                            String usage = (String) med.get("usage");
                            Integer days = med.get("days") != null ? (Integer) med.get("days") : null;
                            Integer quantity = med.get("quantity") != null ? (Integer) med.get("quantity") : null;
                            String displayName = medicineName != null ? medicineName : (genericName != null ? genericName : "未知药品");
                            formattedPlan.append("• ").append(displayName);
                            if (specification != null && !specification.isEmpty()) {
                                formattedPlan.append(" (").append(specification);
                                if (dosageForm != null && !dosageForm.isEmpty()) {
                                    formattedPlan.append(", ").append(dosageForm);
                                }
                                formattedPlan.append(")");
                            } else if (dosageForm != null && !dosageForm.isEmpty()) {
                                formattedPlan.append(" (").append(dosageForm).append(")");
                            }
                            if (frequency != null && !frequency.isEmpty()) {
                                formattedPlan.append("，").append(frequency);
                            }
                            if (usage != null && !usage.isEmpty()) {
                                formattedPlan.append("，").append(usage);
                            }
                            if (days != null && days > 0) {
                                formattedPlan.append("，共").append(days).append("天");
                            }
                            if (quantity != null && quantity > 0 && dosageUnit != null) {
                                formattedPlan.append("，每次").append(quantity).append(dosageUnit);
                            }
                            formattedPlan.append("\n");
                        }
                        if (formattedPlan.length() > 0) {
                            recordDetail.put("treatmentPlan", formattedPlan.toString());
                        } else {
                            recordDetail.put("treatmentPlan", treatmentPlan);
                        }
                    } else {
                        recordDetail.put("treatmentPlan", treatmentPlan);
                    }
                } catch (Exception e) {
                    recordDetail.put("treatmentPlan", treatmentPlan);
                }
            } else {
                recordDetail.put("treatmentPlan", "暂无数据");
            }

            // ========== 获取费用明细 ==========
            try {
                com.example.keshe1.dto.MedicalRecordPriceDTO priceDTO = medicalRecordService.calculateTotalPrice(record.getId());
                if (priceDTO != null) {
                    recordDetail.put("registrationFee", "¥" + priceDTO.getRegistrationFee().setScale(2, BigDecimal.ROUND_HALF_UP));
                    recordDetail.put("medicineFee", "¥" + priceDTO.getMedicineFee().setScale(2, BigDecimal.ROUND_HALF_UP));
                    recordDetail.put("examinationFee", "¥" + priceDTO.getExaminationFee().setScale(2, BigDecimal.ROUND_HALF_UP));
                    recordDetail.put("totalPrice", "¥" + priceDTO.getTotalPrice().setScale(2, BigDecimal.ROUND_HALF_UP));
                } else {
                    recordDetail.put("registrationFee", "¥0.00");
                    recordDetail.put("medicineFee", "¥0.00");
                    recordDetail.put("examinationFee", "¥0.00");
                    recordDetail.put("totalPrice", "¥0.00");
                }
            } catch (Exception e) {
                recordDetail.put("registrationFee", "¥0.00");
                recordDetail.put("medicineFee", "¥0.00");
                recordDetail.put("examinationFee", "¥0.00");
                recordDetail.put("totalPrice", "¥0.00");
            }

            // 获取医生信息
            if (record.getDoctorId() != null) {
                Doctor doctor = doctorService.getById(record.getDoctorId());
                if (doctor != null) {
                    User doctorUser = userService.getById(doctor.getUserId());
                    if (doctorUser != null) {
                        recordDetail.put("doctorName", doctorUser.getRealName());
                    }
                    if (doctor.getDepartmentId() != null) {
                        Department department = departmentService.getById(doctor.getDepartmentId());
                        if (department != null) {
                            recordDetail.put("departmentName", department.getName());
                        }
                    }
                }
            }

            result.put("success", true);
            result.put("record", recordDetail);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "系统错误：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    /**
     * 获取病历费用详情接口
     */
    @GetMapping("/medical-records/{id}/price-detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMedicalRecordPriceDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("msg", "未登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            User user = userService.findByUsername(auth.getName());
            if (user == null) {
                user = userService.findByPhone(auth.getName());
            }
            if (user == null || !"PATIENT".equals(user.getUserType())) {
                result.put("success", false);
                result.put("msg", "用户不存在或不是患者");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            Patient patient = patientService.getByUserId(user.getId());
            if (patient == null) {
                result.put("success", false);
                result.put("msg", "患者信息不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            MedicalRecord record = medicalRecordService.getById(id);
            if (record == null) {
                result.put("success", false);
                result.put("msg", "病历不存在");
                return ResponseEntity.badRequest().body(result);
            }

            if (!record.getPatientId().equals(patient.getId())) {
                result.put("success", false);
                result.put("msg", "无权查看");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
            }

            // ========== 1. 挂号费 ==========
            BigDecimal registrationFee = BigDecimal.ZERO;
            if (record.getDoctorId() != null) {
                Doctor doctor = doctorService.getById(record.getDoctorId());
                if (doctor != null && doctor.getRegistrationFee() != null) {
                    registrationFee = doctor.getRegistrationFee();
                }
            }

            // ========== 2. 药费 ==========
            BigDecimal medicineFee = BigDecimal.ZERO;
            String treatmentPlan = record.getTreatmentPlan();
            if (treatmentPlan != null && treatmentPlan.trim().startsWith("[")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> meds = mapper.readValue(treatmentPlan, List.class);
                    for (Map<String, Object> med : meds) {
                        Object priceObj = med.get("price");
                        if (priceObj != null) {
                            BigDecimal price = new BigDecimal(priceObj.toString());
                            int quantity = 1;
                            if (med.get("quantity") != null) {
                                quantity = (Integer) med.get("quantity");
                            } else if (med.get("totalQuantity") != null) {
                                quantity = (Integer) med.get("totalQuantity");
                            }
                            medicineFee = medicineFee.add(price.multiply(new BigDecimal(quantity)));
                        }
                    }
                } catch (Exception e) {
                    medicineFee = BigDecimal.ZERO;
                }
            }

            // ========== 3. 检查费 ==========
            BigDecimal examinationFee = BigDecimal.ZERO;
            try {
                List<Examination> examinations = examinationService.getByRecordId(record.getId());
                if (examinations != null && !examinations.isEmpty()) {
                    for (Examination exam : examinations) {
                        if (exam != null) {
                            if (exam.getExaminationItemId() != null) {
                                ExaminationItem item = examinationItemService.getById(exam.getExaminationItemId());
                                if (item != null && item.getPrice() != null) {
                                    examinationFee = examinationFee.add(item.getPrice());
                                }
                            } else if (exam.getPrice() != null) {
                                examinationFee = examinationFee.add(exam.getPrice());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                examinationFee = BigDecimal.ZERO;
            }

            BigDecimal totalPrice = registrationFee.add(medicineFee).add(examinationFee);

            // 确保返回的是数字类型
            result.put("success", true);
            result.put("registrationFee", registrationFee.doubleValue());
            result.put("medicineFee", medicineFee.doubleValue());
            result.put("examinationFee", examinationFee.doubleValue());
            result.put("totalPrice", totalPrice.doubleValue());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "系统错误：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 根据科室ID获取该科室的所有医生（不再过滤room_busy）
     */
    @GetMapping("/doctors/onduty")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOnDutyDoctors(@RequestParam Long departmentId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询指定科室下的所有医生（不再添加 room_busy = 1 的条件）
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(departmentId);

        for (Doctor doctor : doctors) {
            Map<String, Object> doctorMap = new HashMap<>();
            doctorMap.put("id", doctor.getId());
            doctorMap.put("employeeId", doctor.getEmployeeId());
            doctorMap.put("title", doctor.getTitle());
            doctorMap.put("specialty", doctor.getSpecialty());

            // 获取医生姓名（从关联的User表）
            if (doctor.getUserId() != null) {
                User doctorUser = userService.getById(doctor.getUserId());
                if (doctorUser != null) {
                    doctorMap.put("name", doctorUser.getRealName());
                } else {
                    doctorMap.put("name", "未知医生");
                }
            } else {
                doctorMap.put("name", "未知医生");
            }

            // 获取科室名称
            if (doctor.getDepartmentId() != null) {
                Department department = departmentService.getById(doctor.getDepartmentId());
                if (department != null) {
                    doctorMap.put("departmentName", department.getName());
                } else {
                    doctorMap.put("departmentName", "未知科室");
                }
            } else {
                doctorMap.put("departmentName", "未知科室");
            }

            result.add(doctorMap);
        }

        return ResponseEntity.ok(result);
    }
    /**
     * 获取所有科室列表
     */
    @GetMapping("/departments/list")
    @ResponseBody
    public ResponseEntity<List<Department>> getDepartments() {
        List<Department> departments = departmentService.list();
        return ResponseEntity.ok(departments);
    }
    /**
     * 获取医生排班信息
     */
    @GetMapping("/doctors/{id}/schedules")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDoctorSchedules(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> schedules = doctorService.getDoctorSchedules(id);
            result.put("success", true);
            result.put("schedules", schedules);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取排班失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    /**
     * 获取医生详细信息
     */
    @GetMapping("/doctors/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDoctorDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            Doctor doctor = doctorService.getById(id);
            if (doctor == null) {
                result.put("success", false);
                result.put("message", "医生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            Map<String, Object> doctorMap = new HashMap<>();
            doctorMap.put("id", doctor.getId());
            doctorMap.put("employeeId", doctor.getEmployeeId());
            doctorMap.put("title", doctor.getTitle());
            doctorMap.put("specialty", doctor.getSpecialty());
            doctorMap.put("introduction", doctor.getIntroduction());

            // 获取医生姓名（从关联的User表）
            if (doctor.getUserId() != null) {
                User doctorUser = userService.getById(doctor.getUserId());
                if (doctorUser != null) {
                    doctorMap.put("name", doctorUser.getRealName());
                }
            }

            // 获取科室名称
            if (doctor.getDepartmentId() != null) {
                Department department = departmentService.getById(doctor.getDepartmentId());
                if (department != null) {
                    doctorMap.put("departmentName", department.getName());
                }
            }

            result.put("success", true);
            result.put("doctor", doctorMap);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取医生信息失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    /**
     * 获取医生某天的诊室排班信息
     */
    @GetMapping("/doctors/{id}/rooms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDoctorRooms(
            @PathVariable Long id,
            @RequestParam String date) {
        Map<String, Object> result = new HashMap<>();

        try {
            Doctor doctor = doctorService.getById(id);
            if (doctor == null) {
                result.put("success", false);
                result.put("message", "医生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            // 修复：处理 12026-02-28 这种特殊格式
            LocalDate scheduleDate;
            if (date.startsWith("120")) {
                // 将 12026-02-28 转换为 2026-02-28
                String fixedDate = "2" + date.substring(2);
                scheduleDate = LocalDate.parse(fixedDate);
                System.out.println("日期格式转换: " + date + " -> " + fixedDate);
            } else {
                scheduleDate = LocalDate.parse(date);
            }

            // 查询该医生在指定日期的排班信息
            List<Map<String, Object>> rooms = doctorService.getDoctorRoomsByDate(id, scheduleDate);

            // 计算剩余号数
            for (Map<String, Object> room : rooms) {
                // 从数据库字段获取总号数和已预约数
                Integer quota = (Integer) room.get("registration_quota");
                Integer registered = (Integer) room.get("registered_count");

                if (quota == null) {
                    quota = 0;
                }
                if (registered == null) {
                    registered = 0;
                }

                // 计算剩余号数
                int remainingSlots = quota - registered;
                room.put("remainingSlots", remainingSlots);

                System.out.println("诊室: " + room.get("room_number") +
                        ", 总号: " + quota +
                        ", 已约: " + registered +
                        ", 剩余: " + remainingSlots);
            }

            result.put("success", true);
            result.put("rooms", rooms);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取诊室信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    }
