package com.example.keshe1.controller;

import com.example.keshe1.dto.*;
import com.example.keshe1.entity.*;
import com.example.keshe1.mapper.AppointmentMapper;
import com.example.keshe1.mapper.MedicalRecordMapper;
import com.example.keshe1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import com.example.keshe1.service.HealthProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private ExaminationItemService examinationItemService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private HealthProfileService healthProfileService;

    @PostMapping("/forgot-password")
    @ResponseBody
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> req) {
        Map<String, Object> result = new HashMap<>();
        String identifier = req.get("identifier");
        String newPassword = req.get("newPassword");
        String confirmPassword = req.get("confirmPassword");
        String hospitalIdStr = req.get("hospitalId");
        if (identifier == null || identifier.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入用户名、手机号或工号");
            return result;
        }
        if (hospitalIdStr == null || hospitalIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择医院");
            return result;
        }
        Long hospitalId;
        try {
            hospitalId = Long.parseLong(hospitalIdStr);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "无效的医院ID");
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
        if (user == null) {
            user = userService.findByEmployeeId(identifier);
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "未找到医生账号");
            return result;
        }
        if (user.getHospitalId() == null || !user.getHospitalId().equals(hospitalId)) {
            result.put("success", false);
            result.put("message", "医院不匹配");
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
     * 显示医生登录页面
     */
    @GetMapping("/doctor/login")
    public String showDoctorLoginPage(@RequestParam(required = false) Long hospitalId, Model model) {
        if (hospitalId != null) {
            model.addAttribute("hospitalId", hospitalId);
        }
        
        // 获取所有医院列表，用于前端下拉选择
        List<Hospital> hospitals = hospitalService.list();
        model.addAttribute("hospitals", hospitals);
        return "redirect:/login";
    }

    /**
     * 处理医生登录
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) Long hospitalId,
            HttpSession session,
            Model model) {
        // 这个方法现在只是一个转发，实际的认证由Spring Security处理
        // 如果到达这里，说明认证失败
        model.addAttribute("error", "用户名或密码错误");
        return "redirect:/login";
    }

    /**
     * 显示绑定电话号码页面
     */
    @GetMapping("/bind-phone")
    public String showBindPhonePage(HttpSession session, Model model) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 获取医生信息
        Doctor doctor = doctorService.getByUserId(user.getId());
        if (doctor == null) {
            return "redirect:/login";
        }
        
        // 如果医生已经绑定了电话，直接跳转到仪表板
        if (Boolean.TRUE.equals(doctor.getPhoneBound())) {
            return "redirect:/doctor/dashboard";
        }

        model.addAttribute("user", user);
        return "doctor/bind-phone";
    }

    /**
     * 处理绑定电话号码
     */
    @PostMapping("/bind-phone")
    public String bindPhone(
            @RequestParam String phone,
            @RequestParam String confirmPhone,
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }
    
        // 验证两次输入的电话号码是否一致
        if (!phone.equals(confirmPhone)) {
            model.addAttribute("error", "两次输入的电话号码不一致");
            return "doctor/bind-phone";
        }
    
        // 验证电话号码格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            model.addAttribute("error", "请输入正确的11位手机号码");
            return "doctor/bind-phone";
        }
    
        // 检查电话号码是否已被其他用户使用
        if (!userService.isPhoneUnique(phone)) {
            model.addAttribute("error", "该电话号码已被其他用户使用");
            return "doctor/bind-phone";
        }
    
        // 更新用户信息
        user.setPhone(phone);
        userService.updateById(user);
    
        // 获取对应的医生信息
        Doctor doctor = doctorService.getByUserId(user.getId());
        if (doctor != null) {
            // 更新医生信息
            doctor.setPhoneBound(true);
            doctorService.updateById(doctor);
                
            session.setAttribute("doctor", doctor);
        }
    
        return "redirect:/doctor/dashboard";
    }

    /**
     * 医生仪表板（首页）
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // 1. 安全检查：从Spring Security获取认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 2. 获取用户并校验身份
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());

        // 修复：使用 getUserType() 判断，而不是 getRole()
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 3. 获取医生档案
        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            doctor = doctorService.getByUserId(user.getId());
            if (doctor != null) {
                session.setAttribute("doctor", doctor);
            }
        }

        // 4. 检查手机号绑定状态 (保留原有逻辑)
        if (doctor != null && !Boolean.TRUE.equals(doctor.getPhoneBound())) {
            return "redirect:/doctor/bind-phone";
        }

        // 5. ================== KPI 核心指标统计 ==================
        if (doctor != null) {
            // 单一事实来源：所有数据统计逻辑都封装在 Service 中
            DoctorDashboardStatsDTO stats = doctorService.getDashboardStats(doctor.getId());

            // 将完整填充的 stats 放入 Model
            model.addAttribute("stats", stats);
            model.addAttribute("doctor", doctor); // 确保页面能用到医生信息
        }

        return "doctor/dashboard"; // 确保这里返回的是正确的模板路径
    }


    /**
     * 医生信息管理页面（完整版）
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            doctor = doctorService.getByUserId(user.getId());
            if (doctor != null) {
                session.setAttribute("doctor", doctor);
            }
        }

        // 如果医生信息不存在，重定向到登录页面
        if (doctor == null) {
            return "redirect:/login";
        }

        // 获取科室信息
        Department department = null;
        if (doctor.getDepartmentId() != null) {
            department = departmentService.getById(doctor.getDepartmentId());
        }

        // 获取医院信息
        Hospital hospital = null;
        if (doctor.getHospitalId() != null) {
            hospital = hospitalService.getById(doctor.getHospitalId());
        }

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("department", department);
        model.addAttribute("hospital", hospital);
        model.addAttribute("timestamp", System.currentTimeMillis());
        return "doctor/profile";
    }

    /**
     * 更新医生信息（处理表单提交） - 使用现有字段
     */
    @PostMapping("/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(
            @RequestParam Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String introduction,
            @RequestParam(required = false) String email,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        // 验证医生身份
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "用户类型错误");
            return result;
        }

        // 获取医生信息
        Doctor doctor = doctorService.getById(id);
        if (doctor == null || !doctor.getUserId().equals(user.getId())) {
            result.put("success", false);
            result.put("message", "无权修改该医生信息");
            return result;
        }

        try {
            // 更新User表（电话和邮箱）
            boolean userUpdated = false;

            if (phone != null && !phone.trim().isEmpty()) {
                // 验证电话号码格式
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    result.put("success", false);
                    result.put("message", "请输入正确的11位手机号码");
                    return result;
                }

                // 检查电话号码是否已被其他用户使用（排除当前用户）
                User existingUserWithPhone = userService.findByPhone(phone);
                if (existingUserWithPhone != null && !existingUserWithPhone.getId().equals(user.getId())) {
                    result.put("success", false);
                    result.put("message", "该电话号码已被其他用户使用");
                    return result;
                }

                user.setPhone(phone);
                userUpdated = true;
            }

            if (email != null && !email.trim().isEmpty()) {
                // 验证邮箱格式
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    result.put("success", false);
                    result.put("message", "请输入正确的邮箱地址");
                    return result;
                }

                user.setEmail(email);
                userUpdated = true;
            }

            if (userUpdated) {
                userService.updateById(user);
            }

            // 使用新的updateDoctorInfo方法更新Doctor表
            Doctor doctorToUpdate = new Doctor();
            doctorToUpdate.setId(id);
            doctorToUpdate.setTitle(title);
            doctorToUpdate.setGender(gender);
            doctorToUpdate.setBirthDate(birthDate);
            doctorToUpdate.setSpecialty(specialty);
            doctorToUpdate.setIntroduction(introduction);

            boolean doctorUpdated = doctorService.updateDoctorInfo(doctorToUpdate);

            if (doctorUpdated || userUpdated) {
                // 同步：若提交了出生日期，则更新用户表中的生日
                if (birthDate != null) {
                    user.setBirthDay(birthDate.toString());
                    userService.updateById(user);
                }
                // 重新获取更新后的医生信息
                Doctor updatedDoctor = doctorService.getById(id);
                if (updatedDoctor != null) {
                    session.setAttribute("doctor", updatedDoctor);
                }

                result.put("success", true);
                result.put("message", "个人信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "没有需要更新的信息");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }


    /**
     * 上传医生头像
     */
    @PostMapping("/avatar/upload")
    @ResponseBody
    public Map<String, Object> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        // 验证医生身份
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "用户类型错误");
            return result;
        }

        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("success", false);
                result.put("message", "只能上传图片文件");
                return result;
            }

            // 验证文件大小（已在配置中限制，这里做二次验证）
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("success", false);
                result.put("message", "文件大小不能超过2MB");
                return result;
            }

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String uniqueFileName = "doctor_avatar_" + user.getId() + "_" +
                    System.currentTimeMillis() + fileExtension;

            // 创建医生头像存储目录
            String uploadDir = "uploads/avatars/doctor_" + user.getId();
            String projectRoot = System.getProperty("user.dir");
            String uploadPath = projectRoot + File.separator + uploadDir;

            // 确保目录存在
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadPath, uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 构建相对路径（用于前端访问）
            String relativePath = "/uploads/avatars/doctor_" + user.getId() + "/" + uniqueFileName;

            // 更新用户头像路径
            boolean updateSuccess = userService.updateAvatar(user.getId(), relativePath);

            if (updateSuccess) {
                // 更新session中的用户信息
                user.setAvatar(relativePath);

                result.put("success", true);
                result.put("message", "头像上传成功");
                result.put("avatarUrl", relativePath);
                result.put("hasAvatar", true);
                result.put("realName", user.getRealName());
            } else {
                result.put("success", false);
                result.put("message", "更新数据库失败");
            }

        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件保存失败: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "上传失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取医生头像信息
     */
    @GetMapping("/avatar/get")
    @ResponseBody
    public Map<String, Object> getAvatarInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 验证医生身份
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "用户类型错误");
            return result;
        }

        result.put("success", true);
        result.put("hasAvatar", user.getAvatar() != null && !user.getAvatar().isEmpty());
        result.put("avatarUrl", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("defaultAvatar", user.getRealName() != null && !user.getRealName().isEmpty() ?
                user.getRealName().substring(0, 1) : "D");
        result.put("realName", user.getRealName());

        return result;
    }


    /**
     * 预约管理页面
     */
    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) String status, HttpSession session, Model model) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            doctor = doctorService.getByUserId(user.getId());
            if (doctor != null) {
                session.setAttribute("doctor", doctor);
            }
        }

        // 添加调试日志
        System.out.println("DEBUG appointments: Current user ID: " + user.getId());
        System.out.println("DEBUG appointments: Current doctor: " + doctor);
        if (doctor != null) {
            System.out.println("DEBUG appointments: Doctor ID: " + doctor.getId());
        }

        // 修复后完整代码段
        List<Appointment> appointments = new ArrayList<>(); // 核心：默认初始化空集合，杜绝null
        if (doctor != null && doctor.getId() != null) { // 先校验doctor和doctorId非空
            if (status != null && !status.trim().isEmpty()) { // 加trim()避免全空格的无效状态
                // 调用服务方法后判空，null则用默认空集合
                List<Appointment> temp = appointmentService.getByDoctorIdAndStatus(doctor.getId(), status);
                appointments = temp != null ? temp : new ArrayList<>();
            } else {
                // 同理：服务返回null则兜底为空集合
                List<Appointment> temp = appointmentService.getByDoctorId(doctor.getId());
                appointments = temp != null ? temp : new ArrayList<>();
            }
        }

        System.out.println("DEBUG appointments: Found appointments count: " + appointments.size() + ", with status filter: " + status);

        // ==================== 新增：为每个预约补充患者信息和科室信息 ====================
        for (Appointment appointment : appointments) {
            if (appointment.getPatientId() != null) {
                Patient patient = patientService.getById(appointment.getPatientId());
                if (patient != null) {
                    // 从关联的用户表获取患者姓名
                    User patientUser = userService.getById(patient.getUserId());
                    if (patientUser != null) {
                        appointment.setPatientName(patientUser.getRealName()); // 设置患者姓名
                    }
                    appointment.setPatientAge(patient.getAge()); // 设置患者年龄
                    appointment.setPatientGender(patient.getGender()); // 设置患者性别
                    // 不需要设置patientPhone和patientIdCard，因为表格中未显示
                }
            }

            if (appointment.getDoctorId() != null) {
                Doctor doc = doctorService.getById(appointment.getDoctorId());
                if (doc != null) {
                    // 获取科室信息
                    if (doc.getDepartmentId() != null) {
                        Department department = departmentService.getById(doc.getDepartmentId());
                        if (department != null) {
                            appointment.setDepartmentName(department.getName());
                        }
                    }
                }
            }
        }
        // ==================== 补充信息结束 ====================

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        model.addAttribute("statusFilter", status);
        return "doctor/appointments";
    }

    /**
     * 预约列表页面（仅显示预约该医生的患者信息）
     */
    @GetMapping("/appointment-list")
    public String appointmentList(HttpSession session, Model model) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            doctor = doctorService.getByUserId(user.getId());
            if (doctor != null) {
                session.setAttribute("doctor", doctor);
            }
        }

        // 添加调试日志
        System.out.println("DEBUG: Current user ID: " + user.getId());
        System.out.println("DEBUG: Current doctor: " + doctor);
        if (doctor != null) {
            System.out.println("DEBUG: Doctor ID: " + doctor.getId());
        }

        // 获取预约该医生的患者信息
        List<Appointment> appointments = new ArrayList<>(); // 默认初始化空集合，避免null
        if (doctor != null && doctor.getId() != null) { // 防御性检查
            List<Appointment> temp = appointmentService.getByDoctorId(doctor.getId());
            appointments = temp != null ? temp : new ArrayList<>();
        }

        System.out.println("DEBUG: Found appointments count: " + appointments.size());

        // 为每个预约补充患者信息和科室信息
        for (Appointment appointment : appointments) {
            if (appointment.getPatientId() != null) {
                Patient patient = patientService.getById(appointment.getPatientId());
                if (patient != null) {
                    // 从关联的用户表获取患者姓名
                    User patientUser = userService.getById(patient.getUserId());
                    if (patientUser != null) {
                        appointment.setPatientName(patientUser.getRealName()); // 设置患者姓名
                    }
                    appointment.setPatientAge(patient.getAge()); // 设置患者年龄
                    appointment.setPatientGender(patient.getGender()); // 设置患者性别
                    appointment.setPatientIdCard(patient.getIdCard()); // 设置患者身份证号
                }
            }

            if (appointment.getDoctorId() != null) {
                Doctor doc = doctorService.getById(appointment.getDoctorId());
                if (doc != null) {
                    // 获取科室信息
                    if (doc.getDepartmentId() != null) {
                        Department department = departmentService.getById(doc.getDepartmentId());
                        if (department != null) {
                            appointment.setDepartmentName(department.getName());
                        }
                    }
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "doctor/appointment-list";
    }

    /**
     * 更新预约状态（通过/拒绝）
     */
    @PostMapping("/appointments/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateAppointmentStatus(
            @RequestParam Long appointmentId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 验证医生身份（从session获取当前医生ID）
        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            result.put("msg", "请先登录");
            result.put("success", "false");
            return ResponseEntity.ok(result);
        }

        // 验证预约是否属于该医生
        Appointment appointment = appointmentService.getById(appointmentId);
        if (appointment == null || !appointment.getDoctorId().equals(doctor.getId())) {
            result.put("msg", "无权操作该预约");
            result.put("success", "false");
            return ResponseEntity.ok(result);
        }

        // 验证当前状态是否为"待确认"（只有"待确认"状态的预约才能被通过或拒绝）
        if (!"待确认".equals(appointment.getStatus())) {
            result.put("msg", "该预约状态无法进行此操作");
            result.put("success", "false");
            return ResponseEntity.ok(result);
        }

        // 验证状态参数是否合法
        if (!("已确认".equals(status) || "已拒绝".equals(status))) {
            result.put("msg", "无效的操作");
            result.put("success", "false");
            return ResponseEntity.ok(result);
        }

        // 更新预约状态
        appointment.setStatus(status);
        if ("已拒绝".equals(status)) {
            appointment.setRejectReason(rejectReason); // 设置拒绝原因
        }

        boolean updated = appointmentService.updateById(appointment);
        if (updated) {
            result.put("msg", "操作成功");
            result.put("success", "true");
        } else {
            result.put("msg", "操作失败");
            result.put("success", "false");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/appointments/detail/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAppointmentDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 验证医生身份（从session获取当前医生ID）
        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return ResponseEntity.ok(result);
        }

        // 验证预约是否属于该医生
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || !appointment.getDoctorId().equals(doctor.getId())) {
            result.put("success", false);
            result.put("msg", "无权查看该预约");
            return ResponseEntity.ok(result);
        }

        // 获取患者信息
        Patient patient = patientService.getById(appointment.getPatientId());

        // 获取医生信息
        Doctor doc = doctorService.getById(appointment.getDoctorId());

        // 获取科室信息
        Department department = null;
        if (doc != null && doc.getDepartmentId() != null) {
            department = departmentService.getById(doc.getDepartmentId());
        }

        // 构建返回数据
        Map<String, Object> appointmentDetail = new HashMap<>();
        appointmentDetail.put("id", appointment.getId());
        // 从关联的用户表获取患者姓名
        User patientUser = patient != null ? userService.getById(patient.getUserId()) : null;
        appointmentDetail.put("patientName", patientUser != null ? patientUser.getRealName() : "未知患者");
        appointmentDetail.put("patientGender", patient != null ? patient.getGender() : null);
        appointmentDetail.put("patientAge", patient != null ? patient.getAge() : null);
        appointmentDetail.put("patientPhone", patientUser != null ? patientUser.getPhone() : null);
        appointmentDetail.put("patientIdCard", patient != null ? patient.getIdCard() : null);
        // 从关联的用户表获取医生姓名
        User doctorUser = doc != null ? userService.getById(doc.getUserId()) : null;
        appointmentDetail.put("doctorName", doctorUser != null ? doctorUser.getRealName() : "未知医生");
        appointmentDetail.put("departmentName", department != null ? department.getName() : "未知科室");
        appointmentDetail.put("appointmentDate", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
        appointmentDetail.put("timeSlot", appointment.getTimeSlot());
        appointmentDetail.put("appointmentTime", appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "");
        appointmentDetail.put("reason", appointment.getReason());
        appointmentDetail.put("notes", appointment.getNotes());
        appointmentDetail.put("status", appointment.getStatus());
        appointmentDetail.put("rejectReason", appointment.getRejectReason());
        appointmentDetail.put("createdTime", appointment.getCreatedTime() != null ? appointment.getCreatedTime().toString() : "");

        result.put("success", true);
        result.put("appointment", appointmentDetail);

        return ResponseEntity.ok(result);
    }

    /**
     * 确认预约（将患者添加到候诊队列）
     */
    @PostMapping("/appointments/confirm/{id}")
    public String confirmAppointment(@PathVariable Long id, HttpSession session) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 更新预约状态为已确认（即进入候诊队列）
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus("已确认");
            appointmentService.updateById(appointment);
        }

        return "redirect:/doctor/waiting-queue?confirmed=true";
    }

    /**
     * 完成预约
     */
    @PostMapping("/appointments/complete/{id}")
    public String completeAppointment(@PathVariable Long id, HttpSession session) {
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
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"DOCTOR".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 更新预约状态为已完成
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus("已完成");
            appointmentService.updateById(appointment);
        }

        return "redirect:/doctor/appointments?completed=true";
    }


    /**
     * 专门用于返回电子病历的 HTML 页面片段
     */
    @GetMapping("/medical-records-view")
    public String medicalRecordsPage() {
        return "doctor/medical-records";
    }

    /**
     * 获取医生所在医院的所有病历记录 (AJAX API)
     * 返回 JSON 格式数据供前端渲染
     */
    @GetMapping("/api/medical-records")
    @ResponseBody
    public ResponseEntity<List<HospitalMedicalRecordDTO>> getHospitalMedicalRecords(
            @RequestParam(required = false) String name,
            HttpSession session) {

        // 1. 获取当前登录信息
        Doctor doctor = (Doctor) session.getAttribute("doctor");

        // 双重检查：如果 Session 中没有，尝试重新从 SecurityContext 获取
        if (doctor == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = userService.findByUsername(auth.getName());
                if (user == null) user = userService.findByPhone(auth.getName());
                if (user == null) user = userService.findByEmployeeId(auth.getName());

                if (user != null) {
                    doctor = doctorService.getByUserId(user.getId());
                    if (doctor != null) {
                        session.setAttribute("doctor", doctor);
                    }
                }
            }
        }

        if (doctor == null) {
            return ResponseEntity.status(401).build(); // 未授权
        }

        // 2. 调用 Service 获取数据 (支持模糊查询)
        try {
            // 将原来的 getHospitalMedicalRecordsByDoctorId 换成新的 searchMedicalRecords
            List<HospitalMedicalRecordDTO> records = medicalRecordService.searchMedicalRecords(doctor.getId(), name);

            return ResponseEntity.ok(records);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取单条病历的详细信息（包含主诉、现病史、既往史等所有字段）
     */
    @GetMapping("/api/medical-records/{id}")
    @ResponseBody
    public ResponseEntity<MedicalRecord> getMedicalRecordDetail(@PathVariable Long id) {
        // 直接使用 MyBatis-Plus 的 getById 查询完整实体
        MedicalRecord record = medicalRecordService.getById(id);

        if (record != null) {
            return ResponseEntity.ok(record);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新病历信息
     */
    @PostMapping("/api/medical-records/update")
    @ResponseBody
    public ResponseEntity<String> updateMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        // 安全检查：确保有 ID
        if (medicalRecord.getId() == null) {
            return ResponseEntity.badRequest().body("病历ID不能为空");
        }

        // 调用 Service 更新 (updateById 会根据 ID 更新非空字段)
        // 建议加上 updateTime 的更新逻辑，或者数据库已配置自动更新
        boolean success = medicalRecordService.updateById(medicalRecord);

        if (success) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.status(500).body("更新失败");
        }
    }


    /**
     * 创建新病历
     */
    @PostMapping("/api/medical-records/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createMedicalRecord(
            @RequestBody CreateMedicalRecordDTO dto,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        // 验证医生身份
        Doctor doctor = (Doctor) session.getAttribute("doctor");
        if (doctor == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = userService.findByUsername(auth.getName());
                if (user == null) user = userService.findByPhone(auth.getName());
                if (user == null) user = userService.findByEmployeeId(auth.getName());

                if (user != null) {
                    doctor = doctorService.getByUserId(user.getId());
                    if (doctor != null) {
                        session.setAttribute("doctor", doctor);
                    }
                }
            }
        }

        if (doctor == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }

        // 验证患者ID
        if (dto.getPatientId() == null) {
            result.put("success", false);
            result.put("message", "请选择患者");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 创建病历
            MedicalRecord newRecord = medicalRecordService.createMedicalRecord(
                    doctor.getId(),
                    dto.getPatientId()
            );

            result.put("success", true);
            result.put("recordId", newRecord.getId());
            result.put("message", "病历创建成功");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 保存处方（治疗计划）并更新总金额
     */
    @PostMapping("/api/prescription/save")
    @ResponseBody
    public ResponseEntity<String> savePrescription(
            @RequestParam Long recordId,
            @RequestParam String prescriptionJson) {

        try {
            boolean success = medicalRecordService.updateTreatmentPlan(recordId, prescriptionJson);

            if (success) {
                // 保存成功后，自动计算并更新总金额
                medicalRecordService.updateTotalPrice(recordId);
                return ResponseEntity.ok("处方保存成功，总金额已更新");
            } else {
                return ResponseEntity.badRequest().body("保存失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("保存失败：" + e.getMessage());
        }
    }

    /**
     * 提交开检查请求（已存在，更新自动计算总金额逻辑）
     */
    @PostMapping("/api/examinations/prescribe")
    @ResponseBody
    public ResponseEntity<String> prescribeExaminations(@RequestBody PrescribeDTO dto) {
        try {
            examinationService.prescribeExaminations(dto.getRecordId(), dto.getItemIds());

            // 开检查后，自动计算并更新总金额
            medicalRecordService.updateTotalPrice(dto.getRecordId());

            return ResponseEntity.ok("开具检查成功，总金额已更新");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取病历费用详情
     */
    @GetMapping("/api/medical-records/{id}/price-detail")
    @ResponseBody
    public ResponseEntity<MedicalRecordPriceDTO> getMedicalRecordPriceDetail(@PathVariable Long id) {
        try {
            MedicalRecordPriceDTO priceDTO = medicalRecordService.calculateTotalPrice(id);
            return ResponseEntity.ok(priceDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取所有患者列表（用于创建病历时的选择）
     */
    @GetMapping("/api/patients/list")
    @ResponseBody
    public ResponseEntity<List<Patient>> getAllPatients() {
        try {
            List<Patient> patients = patientService.list();

            // 为每个患者补充姓名信息
            for (Patient patient : patients) {
                User user = userService.getById(patient.getUserId());
                if (user != null) {
                    patient.setPatientName(user.getRealName()); // 假设Patient类有setPatientName方法
                }
            }

            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }




    // ================== 检查项目相关接口 ==================

    /**
     * 获取所有检查项目（用于前端开检查弹窗的下拉框）
     */
    @GetMapping("/api/examination-items")
    @ResponseBody
    public ResponseEntity<List<ExaminationItem>> getAllExaminationItems() {
        return ResponseEntity.ok(examinationService.getAllExaminationItems());
    }

    /**
     * 获取所有检查项目列表（用于开单下拉框）
     * 修复前端 api/examinations/items 404 错误
     */
    @GetMapping("/api/examinations/items")
    @ResponseBody
    public ResponseEntity<List<ExaminationItem>> getExaminationItems() {
        // 调用 Service 层获取所有检查项目
        return ResponseEntity.ok(examinationItemService.list());
    }

    /**
     *  提交检查结果
     */
    @PostMapping("/api/examinations/result")
    @ResponseBody
    public ResponseEntity<String> submitExaminationResult(@RequestBody ExamResultDTO dto) {
        try {
            examinationService.fillExaminationResult(dto.getId(), dto.getResult());
            return ResponseEntity.ok("结果已保存");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("保存失败");
        }
    }

    /**
     * 页面跳转：检查管理页面
     */
    @GetMapping("/examinations")
    public String examinationPage() {
        return "doctor/examinations";
    }

    /**
     * 获取检查列表（支持状态筛选）
     */
    @GetMapping("/api/examinations/list")
    @ResponseBody
    public ResponseEntity<List<Examination>> getExaminationList(
            @RequestParam(required = false) Long recordId,
            @RequestParam(required = false) String status) {

        // 如果有 recordId，优先使用 recordId 查询
        if (recordId != null) {
            return ResponseEntity.ok(examinationService.getByRecordId(recordId));
        }

        // 如果有状态筛选，使用新的方法
        if (status != null && !status.trim().isEmpty()) {
            return ResponseEntity.ok(examinationService.getDetailsByStatus(status));
        }

        // 否则返回所有
        List<Examination> list = examinationService.getAllDetails();
        return ResponseEntity.ok(list);
    }

    /**
     * 删除检查项目
     */
    @DeleteMapping("/api/examinations/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteExamination(@PathVariable Long id) {
        try {
            examinationService.removeExamination(id);
            return ResponseEntity.ok("删除成功");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("删除失败");
        }
    }


    /**
     * 获取健康档案列表页面（支持局部刷新）
     * @param patientId 可选，根据患者ID筛选
     */
    @GetMapping("/health-profiles")
    public String getHealthProfiles(@RequestParam(required = false) Long patientId, Model model) {
        List<HealthProfile> profiles;

        // 1. 获取档案列表
        if (patientId != null) {
            profiles = healthProfileService.getByPatientId(patientId);
        } else {
            profiles = healthProfileService.list();
        }

        // 2. 【新增】遍历列表，填充病人姓名
        if (profiles != null && !profiles.isEmpty()) {
            for (HealthProfile profile : profiles) {
                // 1. 先查出 Patient 信息
                Patient patient = patientService.getById(profile.getPatientId());

                if (patient != null) {
                    // 2. 获取 Patient 关联的 userId
                    Long userId = patient.getUserId();

                    // 3. 根据 userId 查询 User 信息
                    User user = userService.getById(userId);

                    if (user != null && user.getRealName() != null) {
                        // 4. 将 User 表里的 realName 赋值给 profile 的 patientName
                        profile.setPatientName(user.getRealName());
                    } else {
                        // 如果 User 表没查到或者没有 realName，尝试用 Patient 表里的 patientName (如果有的话)
                        // 或者显示默认值
                        String fallbackName = patient.getPatientName();
                        profile.setPatientName(fallbackName != null ? fallbackName : "未知姓名");
                    }
                } else {
                    profile.setPatientName("未知患者");
                }
            }
        }

        model.addAttribute("profiles", profiles);
        model.addAttribute("currentPatientId", patientId);

        return "doctor/health-profiles";
    }


    /**
     * 【保存或更新健康档案
     * 接收表单数据，如果包含 id 则为修改，否则为新增
     */
    @PostMapping("/health-profiles/save")
    @ResponseBody
    public ResponseEntity<String> saveHealthProfile(HealthProfile healthProfile) {
        try {
            if (healthProfile.getPatientId() == null) {
                return ResponseEntity.badRequest().body("必须填写患者ID");
            }

            // 简单的逻辑：自动计算 BMI
            if (healthProfile.getHeight() != null && healthProfile.getWeight() != null && healthProfile.getHeight() > 0) {
                double heightInMeters = healthProfile.getHeight() / 100.0;
                double bmi = healthProfile.getWeight() / (heightInMeters * heightInMeters);
                // 保留两位小数
                healthProfile.setBmi(Math.round(bmi * 100.0) / 100.0);
            }

            // MyBatis Plus 的 saveOrUpdate 方法：有ID则更新，无ID则插入
            boolean success = healthProfileService.saveOrUpdate(healthProfile);

            if (success) {
                return ResponseEntity.ok("操作成功");
            } else {
                return ResponseEntity.status(500).body("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取单条档案详情（用于编辑回显）
     */
    @GetMapping("/api/health-profiles/{id}")
    @ResponseBody
    public ResponseEntity<HealthProfile> getHealthProfileDetail(@PathVariable Long id) {
        HealthProfile profile = healthProfileService.getById(id);
        return ResponseEntity.ok(profile);
    }


    /**
     * 医生退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
