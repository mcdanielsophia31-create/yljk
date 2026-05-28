package com.example.keshe1.controller;
import com.example.keshe1.dto.DoctorDTO;
import com.example.keshe1.entity.*;
import com.example.keshe1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private HospitalService hospitalService;
    //hsy
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DiagnosisService diagnosisService;
    @Autowired
    private MedicineService medicineService;

    /**
     * 忘记密码（管理员端，未登录）
     */
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            result.put("success", false);
            result.put("message", "未找到管理员账号");
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
     * 药物管理页面
     */
    @GetMapping("/medicines")
    public String medicines(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Boolean ajax,
                          HttpServletRequest request,
                          Model model) {
        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
        }

        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 获取药物数据
        List<Medicine> medicines;
        if (keyword != null && !keyword.isEmpty()) {
            medicines = medicineService.searchMedicines(keyword);
        } else {
            medicines = medicineService.getAllMedicines();
        }

        model.addAttribute("user", user);
        model.addAttribute("medicines", medicines);
        model.addAttribute("searchKeyword", keyword);

        // 检查是否为AJAX请求
        boolean isAjax = Boolean.TRUE.equals(ajax) ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (isAjax) {
            return "admin/medicine-content";
        } else {
            // 如果不是AJAX请求，返回主仪表盘，但带上medicines_view标志
            model.addAttribute("currentView", "medicines");
            return "admin/dashboard";
        }
    }


    /**
     * 显示管理员登录页面
     */
    @GetMapping("/admin/login")
    public String showAdminLoginPage(@RequestParam(required = false) Long hospitalId, Model model) {
        if (hospitalId != null) {
            model.addAttribute("hospitalId", hospitalId);
        }

        // 获取所有医院列表，用于前端下拉选择
        List<Hospital> hospitals = hospitalService.list();
        model.addAttribute("hospitals", hospitals);
        return "redirect:/login";
    }

    /**
     * 处理管理员登录
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
     * 管理员仪表板
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 统计数据
        long patientCount = patientService.count();
        long doctorCount = doctorService.count();
        long departmentCount = departmentService.count();

        model.addAttribute("user", user);
        model.addAttribute("patientCount", patientCount);
        model.addAttribute("doctorCount", doctorCount);
        model.addAttribute("departmentCount", departmentCount);
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/appointment-trend")
    @ResponseBody
    public ResponseEntity<?> getAppointmentTrend(@RequestParam(defaultValue = "7") int days) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            labels.add(start.plusDays(i).format(fmt));
        }
        Map<String, Integer> totalMap = new HashMap<>();
        Map<String, Integer> pendingMap = new HashMap<>();
        Map<String, Integer> confirmedMap = new HashMap<>();
        Map<String, Integer> canceledMap = new HashMap<>();
        {
            QueryWrapper<Appointment> wDate = new QueryWrapper<>();
            wDate.between("appointment_date", start, end);
            List<Appointment> byDate = appointmentService.list(wDate);

            QueryWrapper<Appointment> wTime = new QueryWrapper<>();
            wTime.between("appointment_time", start.atStartOfDay(), end.atTime(23, 59, 59));
            List<Appointment> byTime = appointmentService.list(wTime);

            List<Appointment> merged = new ArrayList<>();
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (Appointment a : byDate) {
                if (a.getId() != null && !seen.contains(a.getId())) {
                    merged.add(a);
                    seen.add(a.getId());
                }
            }
            for (Appointment a : byTime) {
                if (a.getId() == null || !seen.contains(a.getId())) {
                    merged.add(a);
                    if (a.getId() != null) seen.add(a.getId());
                }
            }
            Map<String, java.util.Set<Long>> dayPendingPatients = new HashMap<>();
            Map<String, java.util.Set<Long>> dayConfirmedPatients = new HashMap<>();
            Map<String, java.util.Set<Long>> dayCanceledPatients = new HashMap<>();
            for (Appointment a : merged) {
                if (a.getPatientId() == null) continue;
                if (user.getHospitalId() != null && a.getDoctorId() != null) {
                    Doctor doc = doctorService.getById(a.getDoctorId());
                    boolean match = false;
                    if (doc != null) {
                        match = (doc.getHospitalId() != null && user.getHospitalId().equals(doc.getHospitalId()));
                        if (!match && doc.getUserId() != null) {
                            User du = userService.getById(doc.getUserId());
                            match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                        }
                    }
                    if (!match) continue;
                }
                LocalDate d = a.getAppointmentDate();
                if (d == null && a.getAppointmentTime() != null) {
                    d = a.getAppointmentTime().toLocalDate();
                }
                if (d == null) continue;
                String key = d.format(fmt);
                String status = a.getStatus();
                if ("待确认".equals(status)) {
                    dayPendingPatients.computeIfAbsent(key, k -> new java.util.HashSet<>()).add(a.getPatientId());
                } else if ("已确认".equals(status)) {
                    dayConfirmedPatients.computeIfAbsent(key, k -> new java.util.HashSet<>()).add(a.getPatientId());
                } else if ("已取消".equals(status)) {
                    dayCanceledPatients.computeIfAbsent(key, k -> new java.util.HashSet<>()).add(a.getPatientId());
                }
            }
            for (Map.Entry<String, java.util.Set<Long>> e : dayPendingPatients.entrySet()) {
                pendingMap.put(e.getKey(), e.getValue().size());
            }
            for (Map.Entry<String, java.util.Set<Long>> e : dayConfirmedPatients.entrySet()) {
                confirmedMap.put(e.getKey(), e.getValue().size());
            }
            for (Map.Entry<String, java.util.Set<Long>> e : dayCanceledPatients.entrySet()) {
                canceledMap.put(e.getKey(), e.getValue().size());
            }
            for (String label : labels) {
                java.util.Set<Long> union = new java.util.HashSet<>();
                if (dayPendingPatients.containsKey(label)) union.addAll(dayPendingPatients.get(label));
                if (dayConfirmedPatients.containsKey(label)) union.addAll(dayConfirmedPatients.get(label));
                if (dayCanceledPatients.containsKey(label)) union.addAll(dayCanceledPatients.get(label));
                totalMap.put(label, union.size());
            }
        }
        for (String label : labels) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", label);
            item.put("total", totalMap.getOrDefault(label, 0));
            item.put("pending", pendingMap.getOrDefault(label, 0));
            item.put("confirmed", confirmedMap.getOrDefault(label, 0));
            item.put("canceled", canceledMap.getOrDefault(label, 0));
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard/age-distribution")
    @ResponseBody
    public ResponseEntity<?> getAgeDistribution(@RequestParam(defaultValue = "7") int days) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        List<Patient> patients = new ArrayList<>();
        java.util.Set<Long> patientIdsForHospital = new java.util.HashSet<>();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        {
            QueryWrapper<Appointment> awDate = new QueryWrapper<>();
            awDate.between("appointment_date", start, end);
            List<Appointment> apptsDate = appointmentService.list(awDate);
            QueryWrapper<Appointment> awTime = new QueryWrapper<>();
            awTime.between("appointment_time", start.atStartOfDay(), end.atTime(23, 59, 59));
            List<Appointment> apptsTime = appointmentService.list(awTime);
            List<Appointment> appts = new ArrayList<>();
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (Appointment a : apptsDate) {
                if (a.getId() != null && !seen.contains(a.getId())) {
                    appts.add(a);
                    seen.add(a.getId());
                }
            }
            for (Appointment a : apptsTime) {
                if (a.getId() == null || !seen.contains(a.getId())) {
                    appts.add(a);
                    if (a.getId() != null) seen.add(a.getId());
                }
            }
            for (Appointment a : appts) {
                if (a.getPatientId() == null) continue;
                if (user.getHospitalId() != null && a.getDoctorId() != null) {
                    Doctor doc = doctorService.getById(a.getDoctorId());
                    boolean match = false;
                    if (doc != null) {
                        match = (doc.getHospitalId() != null && user.getHospitalId().equals(doc.getHospitalId()));
                        if (!match && doc.getUserId() != null) {
                            User du = userService.getById(doc.getUserId());
                            match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                        }
                    }
                    if (!match) continue;
                }
                patientIdsForHospital.add(a.getPatientId());
            }
        }
        if (!patientIdsForHospital.isEmpty()) {
            QueryWrapper<Patient> pw = new QueryWrapper<>();
            pw.in("id", patientIdsForHospital);
            patients = patientService.list(pw);
        }
        Map<String, Integer> bucket = new HashMap<>();
        String[] labels = new String[]{"0-12", "13-18", "19-30", "31-45", "46-60", "61-75", "76+"};
        for (String l : labels) bucket.put(l, 0);
        for (Patient p : patients) {
            Integer age = p.getAge();
            if (age == null && p.getUserId() != null) {
                User u = userService.getById(p.getUserId());
                if (u != null && u.getBirthDay() != null) {
                    try {
                        LocalDate bd = LocalDate.parse(u.getBirthDay(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        age = java.time.Period.between(bd, LocalDate.now()).getYears();
                    } catch (Exception ignored) {}
                }
            }
            if (age == null) continue;
            String k;
            if (age <= 12) k = "0-12";
            else if (age <= 18) k = "13-18";
            else if (age <= 30) k = "19-30";
            else if (age <= 45) k = "31-45";
            else if (age <= 60) k = "46-60";
            else if (age <= 75) k = "61-75";
            else k = "76+";
            bucket.put(k, bucket.getOrDefault(k, 0) + 1);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String l : labels) {
            Map<String, Object> m = new HashMap<>();
            m.put("label", l);
            m.put("count", bucket.getOrDefault(l, 0));
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 病人管理页面
     */
    //hsy改
    // 在AdminController.java中修改方法以包含更多患者信息
    // 在AdminController.java中修改方法以包含更多患者信息
    // 在AdminController.java中确保数据传递正确
    // 在AdminController.java中添加日志以检查数据获取
    // 在AdminController.java中修改，只获取最后登录时间和状态
    // 在AdminController.java中修改，将数据存储在data属性中
    // 在AdminController.java中，确保数据正确传递
    @GetMapping("/patients")
    public String patients(@RequestParam(required = false) String name,
                           @RequestParam(required = false) Boolean ajax,
                           HttpServletRequest request,
                           Model model) {
        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
        }

        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        List<Patient> patients;
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            Set<Long> docIds = new java.util.HashSet<>();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) {
                    docIds.add(d.getId());
                }
            }
            Set<Long> patientIds = new java.util.HashSet<>();
            if (!docIds.isEmpty()) {
                QueryWrapper<MedicalRecord> mrw = new QueryWrapper<>();
                mrw.in("doctor_id", docIds);
                List<MedicalRecord> records = medicalRecordService.list(mrw);
                for (MedicalRecord r : records) {
                    if (r.getPatientId() != null) {
                        patientIds.add(r.getPatientId());
                    }
                }
            }
            if (!patientIds.isEmpty()) {
                if (name != null && !name.isEmpty()) {
                    List<Patient> byIds = patientService.list(new QueryWrapper<Patient>().in("id", patientIds));
                    patients = byIds.stream()
                            .filter(p -> {
                                if (p.getUserId() == null) return false;
                                User pu = userService.getById(p.getUserId());
                                String rn = pu != null ? pu.getRealName() : null;
                                return rn != null && rn.contains(name);
                            })
                            .collect(Collectors.toList());
                } else {
                    patients = patientService.list(new QueryWrapper<Patient>().in("id", patientIds));
                }
            } else {
                patients = new ArrayList<>();
            }
        } else {
            if (name != null && !name.isEmpty()) {
                patients = patientService.searchByName(name);
            } else {
                patients = patientService.list();
            }
        }

        // 创建患者详细信息列表
        List<Map<String, Object>> patientInfoList = new ArrayList<>();
        for (Patient patient : patients) {
            Map<String, Object> patientInfo = new HashMap<>();

            // 基本信息从patient表获取
            patientInfo.put("id", patient.getId());
            patientInfo.put("gender", patient.getGender() != null ? patient.getGender() : "未设置");
            patientInfo.put("idCard", patient.getIdCard() != null ? patient.getIdCard() : "未设置");

            // 从关联的user表获取姓名、电话、邮箱
            String patientName = "未设置";
            String patientPhone = "未设置";
            String patientEmail = "未设置";
            String lastLoginTime = "未设置";
            String status = "未设置";

            if (patient.getUserId() != null) {
                User patientUser = userService.getById(patient.getUserId());
                if (patientUser != null) {
                    patientName = patientUser.getRealName() != null ? patientUser.getRealName() : "未设置";
                    patientPhone = patientUser.getPhone() != null ? patientUser.getPhone() : "未设置";
                    patientEmail = patientUser.getEmail() != null ? patientUser.getEmail() : "未设置";

                    // 获取最后登录时间和状态
                    if (patientUser.getLastLoginTime() != null) {
                        lastLoginTime = patientUser.getLastLoginTime().toString();
                    }
                    if (patientUser.getStatus() != null) {
                        status = patientUser.getStatus();
                    } else {
                        status = "正常";
                    }
                }
            }

            patientInfo.put("name", patientName);
            patientInfo.put("phone", patientPhone);
            patientInfo.put("email", patientEmail);
            patientInfo.put("lastLoginTime", lastLoginTime);
            patientInfo.put("status", status);

            patientInfoList.add(patientInfo);
        }

        model.addAttribute("user", user);
        model.addAttribute("patients", patients);
        model.addAttribute("patientInfoList", patientInfoList);
        model.addAttribute("searchName", name);

        // 检查是否为AJAX请求
        boolean isAjax = Boolean.TRUE.equals(ajax) ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (isAjax) {
            return "admin/patient-table :: content";
        } else {
            return "admin/patients";
        }


    }
    /**
     * 获取患者列表（AJAX接口）
     */
    @GetMapping("/patients/ajax")
    @ResponseBody
    public String getPatientsAjax(@RequestParam(required = false) String name,
                                  HttpServletRequest request,
                                  Model model) {
        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            user = userService.findByUsername(username);
            if (user == null) user = userService.findByPhone(username);
            if (user == null) user = userService.findByEmployeeId(username);
        }

        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "<div class='alert alert-danger'>未授权访问</div>";
        }

        List<Patient> patients;
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            Set<Long> docIds = new java.util.HashSet<>();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) {
                    docIds.add(d.getId());
                }
            }
            Set<Long> patientIds = new java.util.HashSet<>();
            if (!docIds.isEmpty()) {
                QueryWrapper<MedicalRecord> mrw = new QueryWrapper<>();
                mrw.in("doctor_id", docIds);
                List<MedicalRecord> records = medicalRecordService.list(mrw);
                for (MedicalRecord r : records) {
                    if (r.getPatientId() != null) {
                        patientIds.add(r.getPatientId());
                    }
                }
            }
            if (!patientIds.isEmpty()) {
                if (name != null && !name.trim().isEmpty()) {
                    List<Patient> byIds = patientService.list(new QueryWrapper<Patient>().in("id", patientIds));
                    patients = byIds.stream()
                            .filter(p -> {
                                if (p.getUserId() == null) return false;
                                User pu = userService.getById(p.getUserId());
                                String rn = pu != null ? pu.getRealName() : null;
                                return rn != null && rn.contains(name);
                            })
                            .collect(Collectors.toList());
                } else {
                    patients = patientService.list(new QueryWrapper<Patient>().in("id", patientIds));
                }
            } else {
                patients = new ArrayList<>();
            }
        } else {
            if (name != null && !name.trim().isEmpty()) {
                patients = patientService.searchByName(name);
            } else {
                patients = patientService.list();
            }
        }

        model.addAttribute("patients", patients);
        model.addAttribute("searchName", name);

        // 返回模板片段
        return "admin/patient-table :: content";
    }
    //详细
    /**
     * 患者详情页面 - 显示患者基本信息和就诊历史
     */
    @GetMapping("/patients/{id}/detail")
    public String patientDetail(@PathVariable Long id, Model model, HttpSession session) {
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 获取患者信息
        Patient patient = patientService.getById(id);
        if (patient == null) {
            model.addAttribute("error", "患者不存在");
            return "admin/patients";
        }

        // 获取关联的用户信息
        User patientUser = null;
        if (patient.getUserId() != null) {
            patientUser = userService.getById(patient.getUserId());
        }

        // 获取患者的就诊历史（病历记录）


        // 获取患者的预约记录
;

        // 获取患者的诊断记录


        // 获取患者的检查报告


        // 获取患者的用药记录
        return "admin/patient-detail";
    }

    /**
     * 医生管理页面
     */
    //hsy
    // 在AdminController.java中添加医生相关方法
    // 在AdminController.java中添加医生相关方法
    @GetMapping("/doctors")
    public String doctors(@RequestParam(required = false) String name,
                          @RequestParam(required = false) Boolean ajax,
                          HttpServletRequest request,
                          Model model) {
        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
        }

        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 获取医生数据 - 按医院过滤
        List<DoctorDTO> doctors;
        if (user.getHospitalId() != null) {
            if (name != null && !name.isEmpty()) {
                doctors = doctorService.getDoctorsWithUserInfoByHospitalIdAndName(user.getHospitalId(), name);
            } else {
                doctors = doctorService.getDoctorsWithUserInfoByHospitalId(user.getHospitalId());
            }
        } else {
            if (name != null && !name.isEmpty()) {
                doctors = doctorService.getDoctorsWithUserInfoByName(name);
            } else {
                doctors = doctorService.getAllDoctorsWithUserInfo();
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("doctors", doctors);
        model.addAttribute("searchName", name);

        // 检查是否为AJAX请求
        boolean isAjax = Boolean.TRUE.equals(ajax) ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (isAjax) {
            return "admin/doctor-table :: content";
        } else {
            return "admin/doctors";
        }
    }

    /**
     * 获取医生列表（AJAX接口）
     */
    @GetMapping("/doctors/ajax")
    @ResponseBody
    public String getDoctorsAjax(@RequestParam(required = false) String name,
                                 HttpServletRequest request,
                                 Model model) {
        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            user = userService.findByUsername(username);
            if (user == null) user = userService.findByPhone(username);
            if (user == null) user = userService.findByEmployeeId(username);
        }

        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "<div class='alert alert-danger'>未授权访问</div>";
        }

        // 获取医生数据 - 按医院过滤
        List<DoctorDTO> doctors;
        if (user.getHospitalId() != null) {
            if (name != null && !name.trim().isEmpty()) {
                doctors = doctorService.getDoctorsWithUserInfoByHospitalIdAndName(user.getHospitalId(), name);
            } else {
                doctors = doctorService.getDoctorsWithUserInfoByHospitalId(user.getHospitalId());
            }
        } else {
            if (name != null && !name.trim().isEmpty()) {
                doctors = doctorService.getDoctorsWithUserInfoByName(name);
            } else {
                doctors = doctorService.getAllDoctorsWithUserInfo();
            }
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("searchName", name);

        // 返回模板片段
        return "admin/doctor-table :: content";
    }
    /**
     * 重置医生密码为123456（管理员）
     */
    @PostMapping("/doctors/{id}/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetDoctorPassword(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User admin = userService.findByUsername(auth.getName());
        if (admin == null) admin = userService.findByPhone(auth.getName());
        if (admin == null) admin = userService.findByEmployeeId(auth.getName());
        if (admin == null || !"ADMIN".equals(admin.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return ResponseEntity.status(404).body("医生不存在");
        }
        if (admin.getHospitalId() != null && doctor.getHospitalId() != null 
                && !admin.getHospitalId().equals(doctor.getHospitalId())) {
            return ResponseEntity.status(403).body("医院不匹配，禁止跨医院重置密码");
        }
        if (doctor.getUserId() == null) {
            return ResponseEntity.badRequest().body("医生未关联用户账号");
        }
        boolean updated = userService.updatePassword(doctor.getUserId(), "123456");
        if (updated) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "密码已重置为123456");
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.status(500).body("重置失败");
        }
    }
    // 更新患者分析相关方法，确保API路径与前端一致


    // ... existing code ...
// ... existing code ...

    @ResponseBody
    @GetMapping("/api/patient-analysis/disease-trend")
    public Map<String, Object> getDiseaseTrendFromDiagnosis(
            HttpSession session,
            @RequestParam(defaultValue = "6") Integer timeRange) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("msg", "未登录或会话已过期");
            return error;
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("msg", "权限不足，仅管理员可访问");
            return error;
        }
        List<Map<String, Object>> data = medicalRecordService.getDiseaseTrendFromMedicalRecord(user.getHospitalId(), timeRange);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/disease-trend-medical-record")
    public Map<String, Object> getDiseaseTrendFromMedicalRecord(
            HttpSession session,
            @RequestParam(defaultValue = "6") Integer timeRange) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("msg", "未登录或会话已过期");
            return error;
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("msg", "权限不足，仅管理员可访问");
            return error;
        }
        List<Map<String, Object>> data = medicalRecordService.getDiseaseTrendFromMedicalRecord(user.getHospitalId(), timeRange);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    @ResponseBody
    @GetMapping("/api/patients/{id}/medical-records")
    public List<Map<String, Object>> getPatientMedicalRecords(@PathVariable Long id) {
        List<MedicalRecord> records = medicalRecordService.listByPatientId(id);
        List<Map<String, Object>> list = new ArrayList<>();
        for (MedicalRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("visitDate", r.getVisitDate());
            m.put("diagnosis", r.getDiagnosis());
            String doctorName = "";
            String departmentName = "";
            if (r.getDoctorId() != null) {
                Doctor d = doctorService.getById(r.getDoctorId());
                if (d != null) {
                    User du = d.getUserId() != null ? userService.getById(d.getUserId()) : null;
                    doctorName = du != null && du.getRealName() != null ? du.getRealName() : doctorName;
                    if (d.getDepartmentId() != null) {
                        Department dep = departmentService.getById(d.getDepartmentId());
                        if (dep != null) departmentName = dep.getName();
                    }
                }
            }
            m.put("doctorName", doctorName);
            m.put("departmentName", departmentName);
            list.add(m);
        }
        return list;
    }

    @ResponseBody
    @GetMapping("/api/medical-records/{id}")
    public ResponseEntity<MedicalRecord> getMedicalRecordDetailForAdmin(@PathVariable Long id) {
        MedicalRecord record = medicalRecordService.getById(id);
        if (record != null) {
            return ResponseEntity.ok(record);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/patients-by-diagnosis")
    public List<Map<String, Object>> getPatientsByDiagnosis(
            @RequestParam String diagnosis,
            @RequestParam(defaultValue = "6") Integer timeRange) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new ArrayList<>();
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return new ArrayList<>();
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(timeRange != null ? timeRange : 6);
        QueryWrapper<MedicalRecord> mrw = new QueryWrapper<>();
        mrw.eq("diagnosis", diagnosis);
        mrw.between("visit_date", start, end);
        List<MedicalRecord> records = medicalRecordService.list(mrw);
        java.util.Set<Long> patientIds = new java.util.HashSet<>();
        for (MedicalRecord r : records) {
            if (r.getPatientId() != null) {
                Patient p = patientService.getById(r.getPatientId());
                if (p != null && p.getUserId() != null) {
                    User pu = userService.getById(p.getUserId());
                    if (pu != null && (user.getHospitalId() == null || user.getHospitalId().equals(pu.getHospitalId()))) {
                        patientIds.add(p.getId());
                    }
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long pid : patientIds) {
            Patient p = patientService.getById(pid);
            if (p == null) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            String name = "";
            if (p.getUserId() != null) {
                User pu = userService.getById(p.getUserId());
                if (pu != null && pu.getRealName() != null) name = pu.getRealName();
            }
            m.put("name", name);
            m.put("gender", p.getGender());
            m.put("age", p.getAge());
            result.add(m);
        }
        return result;
    }

    // 辅助方法：避免重复代码
    private Map<String, Object> createDiseaseEntry(String name, int count) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("diseaseName", name);
        entry.put("count", count);
        return entry;
    }
    // 更新患者分析页面的路由，确保只显示当前医院的数据
    @GetMapping("/patients/analysis")
    public String patientAnalysis(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取用户信息
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "admin/patient-analysis";

    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/monthly-trend")
    public ResponseEntity<?> getMonthlyDiseaseTrend(@RequestParam String month,
                                                    @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        java.time.YearMonth ym;
        try {
            ym = java.time.YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("月份格式错误");
        }
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<String> labels = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            labels.add(d.format(fmt));
        }
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, int[]> seriesMap = new HashMap<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getDiagnosisName() == null || d.getDiagnosisDate() == null) continue;
                String name = d.getDiagnosisName();
                int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(start, d.getDiagnosisDate());
                if (idx < 0 || idx >= labels.size()) continue;
                seriesMap.computeIfAbsent(name, k -> new int[labels.size()])[idx] += 1;
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getDiagnosis() == null || r.getVisitDate() == null) continue;
                String name = r.getDiagnosis();
                int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(start, r.getVisitDate());
                if (idx < 0 || idx >= labels.size()) continue;
                seriesMap.computeIfAbsent(name, k -> new int[labels.size()])[idx] += 1;
            }
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, int[]> e : seriesMap.entrySet()) {
            Map<String, Object> s = new HashMap<>();
            s.put("name", e.getKey());
            List<Integer> data = new ArrayList<>();
            for (int v : e.getValue()) data.add(v);
            s.put("data", data);
            series.add(s);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("series", series);
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/monthly-distribution")
    public ResponseEntity<?> getMonthlyDiseaseDistribution(@RequestParam String month,
                                                           @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        java.time.YearMonth ym;
        try {
            ym = java.time.YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("月份格式错误");
        }
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, Integer> countMap = new HashMap<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getDiagnosisName() == null) continue;
                countMap.put(d.getDiagnosisName(), countMap.getOrDefault(d.getDiagnosisName(), 0) + 1);
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getDiagnosis() == null) continue;
                countMap.put(r.getDiagnosis(), countMap.getOrDefault(r.getDiagnosis(), 0) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("count", e.getValue());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/patients-by-diagnosis-month")
    public ResponseEntity<?> getPatientsByDiagnosisMonth(@RequestParam String diagnosis,
                                                         @RequestParam String month,
                                                         @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        java.time.YearMonth ym;
        try {
            ym = java.time.YearMonth.parse(month);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("月份格式错误");
        }
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        java.util.Set<Long> pidSet = new java.util.HashSet<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.eq("diagnosis_name", diagnosis);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getPatientId() != null) pidSet.add(d.getPatientId());
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.eq("diagnosis", diagnosis);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getPatientId() != null) pidSet.add(r.getPatientId());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (!pidSet.isEmpty()) {
            QueryWrapper<Patient> pw = new QueryWrapper<>();
            pw.in("id", pidSet);
            List<Patient> ps = patientService.list(pw);
            for (Patient p : ps) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                String nameStr = "";
                if (p.getUserId() != null) {
                    User pu = userService.getById(p.getUserId());
                    if (pu != null && pu.getRealName() != null) nameStr = pu.getRealName();
                }
                m.put("name", nameStr);
                m.put("gender", p.getGender());
                m.put("age", p.getAge());
                result.add(m);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/yearly-trend")
    public ResponseEntity<?> getYearlyDiseaseTrend(@RequestParam String year,
                                                   @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        int y;
        try { y = Integer.parseInt(year); } catch (Exception e) { return ResponseEntity.badRequest().body("年份格式错误"); }
        LocalDate start = java.time.Year.of(y).atMonth(1).atDay(1);
        LocalDate end = java.time.Year.of(y).atMonth(12).atEndOfMonth();
        List<String> labels = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM");
        for (int m = 1; m <= 12; m++) labels.add(String.format("%02d", m));
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, int[]> seriesMap = new HashMap<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getDiagnosisName() == null || d.getDiagnosisDate() == null) continue;
                int idx = d.getDiagnosisDate().getMonthValue() - 1;
                seriesMap.computeIfAbsent(d.getDiagnosisName(), k -> new int[labels.size()])[idx] += 1;
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getDiagnosis() == null || r.getVisitDate() == null) continue;
                int idx = r.getVisitDate().getMonthValue() - 1;
                seriesMap.computeIfAbsent(r.getDiagnosis(), k -> new int[labels.size()])[idx] += 1;
            }
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, int[]> e : seriesMap.entrySet()) {
            Map<String, Object> s = new HashMap<>();
            s.put("name", e.getKey());
            List<Integer> data = new ArrayList<>();
            for (int v : e.getValue()) data.add(v);
            s.put("data", data);
            series.add(s);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("series", series);
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/yearly-distribution")
    public ResponseEntity<?> getYearlyDiseaseDistribution(@RequestParam String year,
                                                          @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        int y;
        try { y = Integer.parseInt(year); } catch (Exception e) { return ResponseEntity.badRequest().body("年份格式错误"); }
        LocalDate start = java.time.Year.of(y).atMonth(1).atDay(1);
        LocalDate end = java.time.Year.of(y).atMonth(12).atEndOfMonth();
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, Integer> countMap = new HashMap<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getDiagnosisName() == null) continue;
                countMap.put(d.getDiagnosisName(), countMap.getOrDefault(d.getDiagnosisName(), 0) + 1);
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getDiagnosis() == null) continue;
                countMap.put(r.getDiagnosis(), countMap.getOrDefault(r.getDiagnosis(), 0) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("count", e.getValue());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/patients-by-diagnosis-year")
    public ResponseEntity<?> getPatientsByDiagnosisYear(@RequestParam String diagnosis,
                                                        @RequestParam String year,
                                                        @RequestParam(defaultValue = "diagnosis") String source) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        int y;
        try { y = Integer.parseInt(year); } catch (Exception e) { return ResponseEntity.badRequest().body("年份格式错误"); }
        LocalDate start = java.time.Year.of(y).atMonth(1).atDay(1);
        LocalDate end = java.time.Year.of(y).atMonth(12).atEndOfMonth();
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        java.util.Set<Long> pidSet = new java.util.HashSet<>();
        if ("diagnosis".equalsIgnoreCase(source)) {
            QueryWrapper<Diagnosis> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.eq("diagnosis_name", diagnosis);
            qw.between("diagnosis_date", start, end);
            List<Diagnosis> list = diagnosisService.list(qw);
            for (Diagnosis d : list) {
                if (d.getPatientId() != null) pidSet.add(d.getPatientId());
            }
        } else {
            QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
            if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
            qw.eq("diagnosis", diagnosis);
            qw.between("visit_date", start, end);
            List<MedicalRecord> list = medicalRecordService.list(qw);
            for (MedicalRecord r : list) {
                if (r.getPatientId() != null) pidSet.add(r.getPatientId());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (!pidSet.isEmpty()) {
            QueryWrapper<Patient> pw = new QueryWrapper<>();
            pw.in("id", pidSet);
            List<Patient> ps = patientService.list(pw);
            for (Patient p : ps) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                String nameStr = "";
                if (p.getUserId() != null) {
                    User pu = userService.getById(p.getUserId());
                    if (pu != null && pu.getRealName() != null) nameStr = pu.getRealName();
                }
                m.put("name", nameStr);
                m.put("gender", p.getGender());
                m.put("age", p.getAge());
                result.add(m);
            }
        }
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/range-trend")
    public ResponseEntity<?> getRangeDiseaseTrend(@RequestParam String start,
                                                  @RequestParam String end) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        LocalDate s, e;
        try { s = LocalDate.parse(start); e = LocalDate.parse(end); } catch (Exception ex) { return ResponseEntity.badRequest().body("日期格式错误"); }
        if (e.isBefore(s)) return ResponseEntity.badRequest().body("结束时间早于开始时间");
        List<String> labels = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) labels.add(d.format(fmt));
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, int[]> seriesMap = new HashMap<>();
        QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
        if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
        qw.between("visit_date", s, e);
        List<MedicalRecord> list = medicalRecordService.list(qw);
        for (MedicalRecord r : list) {
            if (r.getDiagnosis() == null || r.getVisitDate() == null) continue;
            int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(s, r.getVisitDate());
            if (idx < 0 || idx >= labels.size()) continue;
            seriesMap.computeIfAbsent(r.getDiagnosis(), k -> new int[labels.size()])[idx] += 1;
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, int[]> e1 : seriesMap.entrySet()) {
            Map<String, Object> sObj = new HashMap<>();
            sObj.put("name", e1.getKey());
            List<Integer> data = new ArrayList<>();
            for (int v : e1.getValue()) data.add(v);
            sObj.put("data", data);
            series.add(sObj);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("series", series);
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/range-distribution")
    public ResponseEntity<?> getRangeDiseaseDistribution(@RequestParam String start,
                                                         @RequestParam String end) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        LocalDate s, e;
        try { s = LocalDate.parse(start); e = LocalDate.parse(end); } catch (Exception ex) { return ResponseEntity.badRequest().body("日期格式错误"); }
        if (e.isBefore(s)) return ResponseEntity.badRequest().body("结束时间早于开始时间");
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        Map<String, Integer> countMap = new HashMap<>();
        QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
        if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
        qw.between("visit_date", s, e);
        List<MedicalRecord> list = medicalRecordService.list(qw);
        for (MedicalRecord r : list) {
            if (r.getDiagnosis() == null) continue;
            countMap.put(r.getDiagnosis(), countMap.getOrDefault(r.getDiagnosis(), 0) + 1);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e1 : countMap.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e1.getKey());
            m.put("count", e1.getValue());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @GetMapping("/api/patient-analysis/patients-by-diagnosis-range")
    public ResponseEntity<?> getPatientsByDiagnosisRange(@RequestParam String diagnosis,
                                                         @RequestParam String start,
                                                         @RequestParam String end) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        LocalDate s, e;
        try { s = LocalDate.parse(start); e = LocalDate.parse(end); } catch (Exception ex) { return ResponseEntity.badRequest().body("日期格式错误"); }
        if (e.isBefore(s)) return ResponseEntity.badRequest().body("结束时间早于开始时间");
        java.util.Set<Long> docIds = new java.util.HashSet<>();
        if (user.getHospitalId() != null) {
            List<Doctor> allDoctors = doctorService.list();
            for (Doctor d : allDoctors) {
                boolean match = (d.getHospitalId() != null && user.getHospitalId().equals(d.getHospitalId()));
                if (!match && d.getUserId() != null) {
                    User du = userService.getById(d.getUserId());
                    match = (du != null && du.getHospitalId() != null && user.getHospitalId().equals(du.getHospitalId()));
                }
                if (match) docIds.add(d.getId());
            }
        }
        java.util.Set<Long> pidSet = new java.util.HashSet<>();
        QueryWrapper<MedicalRecord> qw = new QueryWrapper<>();
        if (!docIds.isEmpty()) qw.in("doctor_id", docIds);
        qw.eq("diagnosis", diagnosis);
        qw.between("visit_date", s, e);
        List<MedicalRecord> list = medicalRecordService.list(qw);
        for (MedicalRecord r : list) {
            if (r.getPatientId() != null) pidSet.add(r.getPatientId());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (!pidSet.isEmpty()) {
            QueryWrapper<Patient> pw = new QueryWrapper<>();
            pw.in("id", pidSet);
            List<Patient> ps = patientService.list(pw);
            for (Patient p : ps) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                String nameStr = "";
                if (p.getUserId() != null) {
                    User pu = userService.getById(p.getUserId());
                    if (pu != null && pu.getRealName() != null) nameStr = pu.getRealName();
                }
                m.put("name", nameStr);
                m.put("gender", p.getGender());
                m.put("age", p.getAge());
                result.add(m);
            }
        }
        return ResponseEntity.ok(result);
    }
    // ================== 药物管理 API ==================

    // 获取药物列表API
    @GetMapping("/medicines/list")
    @ResponseBody
    public List<Medicine> getMedicineList(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return medicineService.searchMedicines(keyword);
        }
        return medicineService.getAllMedicines();
    }

    // 保存药物（新增或修改）
    @PostMapping("/medicines/save")
    @ResponseBody
    public ResponseEntity<?> saveMedicine(@RequestBody Medicine medicine) {
        try {
            boolean isNew = medicine.getId() == null;
            if (isNew) {
                medicine.setCreatedTime(LocalDateTime.now());
            }
            medicine.setUpdatedTime(LocalDateTime.now());
            
            boolean success = medicineService.saveOrUpdate(medicine);
            if (success) {
                return ResponseEntity.ok().body("保存成功");
            } else {
                return ResponseEntity.badRequest().body("保存失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("保存出错: " + e.getMessage());
        }
    }

    // 删除药物
    @DeleteMapping("/medicines/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMedicine(@PathVariable Long id) {
        try {
            boolean success = medicineService.removeById(id);
            if (success) {
                return ResponseEntity.ok().body("删除成功");
            } else {
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除出错: " + e.getMessage());
        }
    }
    
    // 获取单个药物信息
    @GetMapping("/medicines/{id}")
    @ResponseBody
    public ResponseEntity<?> getMedicine(@PathVariable Long id) {
        Medicine medicine = medicineService.getById(id);
        if (medicine != null) {
            return ResponseEntity.ok(medicine);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    /**
     * 显示创建医生页面
     */
    @GetMapping("/doctors/create")
    public String showCreateDoctorPage1(HttpSession session, Model model) {
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        // 获取所有科室
        List<Department> departments = departmentService.list();

        model.addAttribute("user", user);
        model.addAttribute("departments", departments);
        return "admin/create-doctor";
    }

    /**
     * 处理创建医生
     */
    // ... existing code ...

    /**
     * 创建医生页面
     */
    @GetMapping("/create-doctor")
    public String showCreateDoctorPage(HttpSession session, Model model) {
        // 从session获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "admin/create-doctor";
    }

// ... existing code ...

    /**
     * 显示编辑医生页面
     */
    @GetMapping("/doctors/edit/{id}")
    public String showEditDoctorPage(@PathVariable Long id, HttpSession session, Model model) {
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return "redirect:/admin/doctors";
        }
        if (user.getHospitalId() != null && doctor.getHospitalId() != null 
                && !user.getHospitalId().equals(doctor.getHospitalId())) {
            return "redirect:/admin/doctors?error=hospital_mismatch";
        }

        // 获取用户信息
        User doctorUser = userService.getById(doctor.getUserId());

        // 获取所有科室
        List<Department> departments = departmentService.list();

        model.addAttribute("user", user);
        model.addAttribute("doctor", doctor);
        model.addAttribute("doctorUser", doctorUser);
        model.addAttribute("departments", departments);
        return "admin/edit-doctor";
    }

    /**
     * 处理编辑医生
     */
    @PostMapping("/doctors/edit/{id}")
    public String editDoctor(
            @PathVariable Long id,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Long departmentId,
            @RequestParam String title,
            @RequestParam String specialty,
            HttpSession session) {

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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return "redirect:/admin/doctors";
        }
        if (user.getHospitalId() != null && doctor.getHospitalId() != null 
                && !user.getHospitalId().equals(doctor.getHospitalId())) {
            return "redirect:/admin/doctors?error=hospital_mismatch";
        }

        // 更新医生信息

        doctor.setDepartmentId(departmentId);
        doctor.setTitle(title);
        doctor.setSpecialty(specialty);

        doctorService.updateById(doctor);

        // 更新用户信息
        User doctorUser = userService.getById(doctor.getUserId());
        doctorUser.setPhone(phone);
        doctorUser.setEmail(email);
        userService.updateById(doctorUser);

        return "redirect:/admin/doctors?updated=true";
    }

    /**
     * 删除医生
     */
    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id, HttpSession session) {
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
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Doctor doctor = doctorService.getById(id);
        if (doctor == null) {
            return "redirect:/admin/doctors";
        }
        if (user.getHospitalId() != null && doctor.getHospitalId() != null 
                && !user.getHospitalId().equals(doctor.getHospitalId())) {
            return "redirect:/admin/doctors?error=hospital_mismatch";
        }

        // 先删除医生信息
        doctorService.removeById(id);

        // 再删除用户账号
        userService.removeById(doctor.getUserId());

        return "redirect:/admin/doctors?deleted=true";
    }

    /**
     * 科室管理页面
     */
    @GetMapping("/departments")
    public String departments(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Boolean ajax,
                              HttpServletRequest request,
                              Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        List<Department> departments;
        if (user.getHospitalId() != null) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Department> wrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("hospital_id", user.getHospitalId())
                        .like("name", keyword.trim());
                departments = departmentService.list(wrapper);
            } else {
                departments = departmentService.getDepartmentsByHospitalId(user.getHospitalId());
            }
        } else {
            if (keyword != null && !keyword.trim().isEmpty()) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Department> wrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.like("name", keyword.trim());
                departments = departmentService.list(wrapper);
            } else {
                departments = departmentService.list();
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("departments", departments);
        model.addAttribute("searchKeyword", keyword);

        boolean isAjax = Boolean.TRUE.equals(ajax) ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        if (isAjax) {
            return "admin/department-content";
        } else {
            model.addAttribute("currentView", "departments");
            return "admin/dashboard";
        }
    }

    /**
     * 创建科室
     */
    @PostMapping("/departments/create")
    public String createDepartment(
            @RequestParam String name,
            @RequestParam String description,
            HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            user = userService.findByPhone(auth.getName());
        }
        if (user == null) {
            user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setHospitalId(user.getHospitalId());

        departmentService.save(department);

        return "redirect:/admin/departments?created=true";
    }

    // ================== 科室管理 API ==================

    @GetMapping("/departments/list")
    @ResponseBody
    public List<Department> getDepartmentList(@RequestParam(required = false) String keyword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
        }
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return new ArrayList<>();
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Department> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (user.getHospitalId() != null) {
            wrapper.eq("hospital_id", user.getHospitalId());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("name", keyword.trim());
        }
        wrapper.orderByDesc("created_time");
        return departmentService.list(wrapper);
    }

    @PostMapping("/departments/save")
    @ResponseBody
    public ResponseEntity<?> saveDepartment(@RequestBody Department department) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(401).body("未登录");
            }
            User user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
            if (user == null || !"ADMIN".equals(user.getUserType())) {
                return ResponseEntity.status(403).body("无权限");
            }
            if (department.getId() == null) {
                department.setHospitalId(user.getHospitalId());
                department.setCreatedTime(LocalDateTime.now());
            }
            department.setUpdatedTime(LocalDateTime.now());

            boolean success = departmentService.saveOrUpdate(department);
            if (success) {
                return ResponseEntity.ok("保存成功");
            } else {
                return ResponseEntity.badRequest().body("保存失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("保存出错: " + e.getMessage());
        }
    }

    @DeleteMapping("/departments/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(401).body("未登录");
            }
            User user = userService.findByUsername(auth.getName());
            if (user == null) user = userService.findByPhone(auth.getName());
            if (user == null) user = userService.findByEmployeeId(auth.getName());
            if (user == null || !"ADMIN".equals(user.getUserType())) {
                return ResponseEntity.status(403).body("无权限");
            }
            boolean success = departmentService.removeById(id);
            if (success) {
                return ResponseEntity.ok("删除成功");
            } else {
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除出错: " + e.getMessage());
        }
    }

    @GetMapping("/departments/{id}")
    @ResponseBody
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        if (dept != null) {
            return ResponseEntity.ok(dept);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/departments/stats")
    @ResponseBody
    public ResponseEntity<?> getDepartmentStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Department> deptWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (user.getHospitalId() != null) {
            deptWrapper.eq("hospital_id", user.getHospitalId());
        }
        deptWrapper.orderByAsc("id");
        List<Department> departments = departmentService.list(deptWrapper);

        List<Map<String, Object>> stats = new ArrayList<>();
        for (Department dep : departments) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Doctor> dw =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            dw.eq("department_id", dep.getId());
            if (user.getHospitalId() != null) {
                dw.eq("hospital_id", user.getHospitalId());
            }
            int doctorCount = (int) doctorService.count(dw);
            Map<String, Object> item = new HashMap<>();
            item.put("departmentId", dep.getId());
            item.put("departmentName", dep.getName());
            item.put("doctorCount", doctorCount);
            stats.add(item);
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/departments/{id}/doctors")
    @ResponseBody
    public ResponseEntity<?> getDepartmentDoctors(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Doctor> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("department_id", id);
        if (user.getHospitalId() != null) {
            wrapper.eq("hospital_id", user.getHospitalId());
        }
        List<Doctor> doctors = doctorService.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Doctor d : doctors) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("userId", d.getUserId());
            m.put("departmentId", d.getDepartmentId());
            m.put("title", d.getTitle());
            m.put("specialty", d.getSpecialty());
            User u = d.getUserId() != null ? userService.getById(d.getUserId()) : null;
            m.put("name", u != null ? u.getRealName() : null);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/departments/move-doctor")
    @ResponseBody
    public ResponseEntity<?> moveDoctor(@RequestParam Long doctorId,
                                        @RequestParam Long targetDepartmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("未登录");
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return ResponseEntity.status(403).body("无权限");
        }
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            return ResponseEntity.badRequest().body("医生不存在");
        }
        Department target = departmentService.getById(targetDepartmentId);
        if (target == null) {
            return ResponseEntity.badRequest().body("目标科室不存在");
        }
        if (user.getHospitalId() != null) {
            if (doctor.getHospitalId() == null || !user.getHospitalId().equals(doctor.getHospitalId())) {
                return ResponseEntity.status(403).body("医院不匹配，禁止跨医院调整医生");
            }
            if (target.getHospitalId() == null || !user.getHospitalId().equals(target.getHospitalId())) {
                return ResponseEntity.status(403).body("目标科室不属于当前医院");
            }
        }
        doctor.setDepartmentId(targetDepartmentId);
        boolean ok = doctorService.updateById(doctor);
        if (ok) {
            return ResponseEntity.ok("调整成功");
        } else {
            return ResponseEntity.badRequest().body("调整失败");
        }
    }

    @GetMapping("/departments/stats-page")
    public String departmentStatsPage(@RequestParam(required = false) Boolean ajax,
                                      HttpServletRequest request,
                                      Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(auth.getName());
        if (user == null) user = userService.findByPhone(auth.getName());
        if (user == null) user = userService.findByEmployeeId(auth.getName());
        if (user == null || !"ADMIN".equals(user.getUserType())) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        boolean isAjax = Boolean.TRUE.equals(ajax) ||
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        if (isAjax) {
            return "admin/department-stats-content";
        } else {
            model.addAttribute("currentView", "departments-stats");
            return "admin/dashboard";
        }
    }

    /**
     * 管理员退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
