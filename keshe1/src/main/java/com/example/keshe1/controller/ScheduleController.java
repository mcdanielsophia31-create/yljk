package com.example.keshe1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.keshe1.entity.ConsultingRoom;
import com.example.keshe1.entity.Department;
import com.example.keshe1.entity.DoctorSchedule;
import com.example.keshe1.entity.RoomBusy;
import com.example.keshe1.entity.User;
import com.example.keshe1.service.ConsultingRoomService;
import com.example.keshe1.service.DepartmentService;
import com.example.keshe1.service.DoctorScheduleService;
import com.example.keshe1.service.DoctorService;
import com.example.keshe1.service.RoomBusyService;
import com.example.keshe1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    
    @Autowired
    private DoctorScheduleService doctorScheduleService;
    
    @Autowired
    private ConsultingRoomService consultingRoomService;
    
    @Autowired
    private RoomBusyService roomBusyService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/overall")
    public String overallSchedule(HttpSession session, Model model, HttpServletRequest request) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        // 获取当前用户的医院ID
        String username = auth.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 查询该医院的科室信息
        List<Department> departments = departmentService.getDepartmentsByHospitalId(currentUser.getHospitalId());

        // 将科室数据添加到模型中
        model.addAttribute("departments", departments);

        // 如果存在科室，设置默认选中的科室ID
        if (departments != null && !departments.isEmpty()) {
            model.addAttribute("defaultDepartmentId", departments.get(0).getId());
        }

        // 获取所有排班信息
        List<DoctorSchedule> schedules = doctorScheduleService.list();
        model.addAttribute("schedules", schedules);

        // 检查是否为AJAX请求
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        // 根据用户角色返回不同页面
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            if (isAjax) {
                // 如果是AJAX请求，只返回内容部分（不包含布局）
                return "admin/overall-schedule-content";
            } else {
                // 直接访问时返回完整页面
                return "admin/overall-schedule";
            }
        } else {
            if (isAjax) {
                // 非管理员的AJAX请求也返回内容部分
                return "schedule/overall-schedule-content";
            } else {
                // 非管理员直接访问返回完整页面
                return "schedule/overall-schedule-common";
            }
        }
    }

    @GetMapping("/departments")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDepartments() {
        System.out.println("=== 开始获取科室列表 ===");

        // 从Spring Security上下文中获取当前用户医院ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            System.out.println("用户未认证或匿名用户");
            return ResponseEntity.ok(new ArrayList<>());
        }

        String username = auth.getName();
        System.out.println("当前登录用户名: " + username);

        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            System.out.println("未找到用户: " + username);
            return ResponseEntity.ok(new ArrayList<>());
        }

        Long hospitalId = currentUser.getHospitalId();
        System.out.println("当前用户医院ID: " + hospitalId);

        // 查询该医院的科室信息
        List<Department> departments = departmentService.getDepartmentsByHospitalId(hospitalId);
        System.out.println("查询到的科室数量: " + (departments != null ? departments.size() : 0));

        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        if (departments != null) {
            for (Department dept : departments) {
                System.out.println("科室 - ID: " + dept.getId() + ", 名称: " + dept.getName());
                Map<String, Object> deptMap = new HashMap<>();
                deptMap.put("id", dept.getId());
                deptMap.put("name", dept.getName());
                result.add(deptMap);
            }
        }

        System.out.println("返回的科室数据: " + result);
        System.out.println("=== 结束获取科室列表 ===");

        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取指定科室的医生列表
     */
    @GetMapping("/doctors/{departmentId}")
    @ResponseBody
    public List<User> getDoctorsByDepartment(@PathVariable Long departmentId) {
        // 从Spring Security上下文中获取当前用户医院ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new ArrayList<>();
        }
        
        // 获取当前用户的医院ID
        String username = auth.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // 查询该科室下的医生用户信息
        return userService.getDoctorsByDepartmentAndHospital(departmentId, currentUser.getHospitalId());
    }
    
    /**
     * 获取指定日期范围内的排班信息
     */
    @GetMapping("/schedules")
    @ResponseBody
    public List<DoctorSchedule> getSchedules(@RequestParam String startDate, 
                                            @RequestParam String endDate, 
                                            @RequestParam Long departmentId) {
        // 从Spring Security上下文中获取当前用户医院ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new ArrayList<>();
        }
        
        // 获取当前用户的医院ID
        String username = auth.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // 查询指定日期范围和科室的排班信息
        return doctorScheduleService.getSchedulesByDateRangeAndDepartment(
            startDate, endDate, departmentId, currentUser.getHospitalId());
    }
    
    /**
     * 获取诊室占用情况
     */
    @GetMapping("/room-busy")
    @ResponseBody
    public List<RoomBusy> getRoomBusy(@RequestParam String scheduleDate, 
                                     @RequestParam String timeSlot, 
                                     @RequestParam Long departmentId) {
        // 从Spring Security上下文中获取当前用户医院ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new ArrayList<>();
        }
        
        // 获取当前用户的医院ID
        String username = auth.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return new ArrayList<>();
        }
        
        // 查询指定日期、时段和科室的诊室占用情况
        return roomBusyService.getRoomBusyByDateAndTimeSlot(
            scheduleDate, timeSlot, departmentId, currentUser.getHospitalId());
    }
    
    /**
     * 更新排班信息
     */
    @PostMapping("/schedules")
    @ResponseBody
    public String updateSchedules(@RequestBody Map<String, Object> requestData) {
        try {
            // 从请求数据中提取参数
            String scheduleDate = (String) requestData.get("scheduleDate");
            String timeSlot = (String) requestData.get("timeSlot");
            // 转换时间段格式以匹配不同表中的枚举值定义
            // room_busy表和doctor_schedule表的time_slot都定义为enum('上午','下午')，不需要前导空格
            String roomBusyTimeSlot = timeSlot; // room_busy表不需要前导空格
            String doctorScheduleTimeSlot = timeSlot; // doctor_schedule表不需要前导空格
            Long departmentId = Long.valueOf(requestData.get("departmentId").toString());
            List<Map<String, Object>> doctors = (List<Map<String, Object>>) requestData.get("doctors");
            
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return "error:unauthorized";
            }
            
            // 获取当前用户的医院ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                return "error:user_not_found";
            }
            
            Long hospitalId = currentUser.getHospitalId();
            
            // 1. 更新room_busy表：将满足条件的记录的employee_id字段值改为空，is_free字段值改为1
            LambdaQueryWrapper<RoomBusy> roomBusyUpdateWrapper = new LambdaQueryWrapper<RoomBusy>()
                .eq(RoomBusy::getScheduleDate, LocalDate.parse(scheduleDate))
                .eq(RoomBusy::getTimeSlot, roomBusyTimeSlot)
                .eq(RoomBusy::getDepartmentId, departmentId)
                .eq(RoomBusy::getHospitalId, hospitalId);
            
            // 查询当前已有的room_busy记录
            List<RoomBusy> existingRoomBusy = roomBusyService.list(roomBusyUpdateWrapper);
            
            // 如果不存在记录，则从consulting_room表查询并插入到room_busy表
            if (existingRoomBusy.isEmpty()) {
                List<ConsultingRoom> rooms = consultingRoomService.getRoomsByDepartmentAndHospital(departmentId, hospitalId);
                
                for (ConsultingRoom room : rooms) {
                    RoomBusy rb = new RoomBusy();
                    rb.setRoomNumber(room.getRoomNumber());
                    rb.setHospitalId(room.getHospitalId());
                    rb.setDepartmentId(room.getDepartmentId());
                    rb.setScheduleDate(LocalDate.parse(scheduleDate));
                    rb.setTimeSlot(roomBusyTimeSlot);
                    rb.setIsFree(true); // 默认设置为可用
                    rb.setEmployeeId(null); // 初始为空
                    roomBusyService.save(rb);
                }
            } else {
                // 如果存在记录，更新employee_id为null，is_free为true
                LambdaUpdateWrapper<RoomBusy> updateWrapper = new LambdaUpdateWrapper<RoomBusy>()
                    .eq(RoomBusy::getScheduleDate, LocalDate.parse(scheduleDate))
                    .eq(RoomBusy::getTimeSlot, roomBusyTimeSlot)
                    .eq(RoomBusy::getDepartmentId, departmentId)
                    .eq(RoomBusy::getHospitalId, hospitalId)
                    .set(RoomBusy::getEmployeeId, null)
                    .set(RoomBusy::getIsFree, true);
                roomBusyService.update(updateWrapper);
            }
            
            // 2. 删除doctor_schedule表中满足条件的记录
            LambdaQueryWrapper<DoctorSchedule> deleteWrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                .eq(DoctorSchedule::getTimeSlot, doctorScheduleTimeSlot)
                .eq(DoctorSchedule::getDepartmentId, departmentId)
                .eq(DoctorSchedule::getHospitalId, hospitalId)
                .eq(DoctorSchedule::getScheduleStatus, "VALID");
            doctorScheduleService.remove(deleteWrapper);
            
            // 3. 为选中的医生插入新的doctor_schedule记录
            for (Map<String, Object> doctorData : doctors) {
                String doctorName = (String) doctorData.get("doctorName");
                String employeeId = (String) doctorData.get("employeeId");
                Integer registrationQuota = Integer.valueOf(doctorData.get("registrationQuota").toString());
                
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setScheduleDate(LocalDate.parse(scheduleDate));
                schedule.setTimeSlot(doctorScheduleTimeSlot);
                schedule.setDoctorName(doctorName);
                schedule.setEmployeeId(employeeId);
                schedule.setRegistrationQuota(registrationQuota);
                schedule.setDepartmentId(departmentId);
                schedule.setHospitalId(hospitalId);
                schedule.setScheduleStatus("VALID");
                schedule.setRegisteredCount(0);
                
                // 查询医生的挂号费用
                // 这里需要根据实际的Doctor实体和查询逻辑来实现
                schedule.setRegistrationFee(new BigDecimal("20.0")); // 暂时设置默认值
                
                doctorScheduleService.save(schedule);
            }
            
            // 4. 分配诊室给医生
            if (!doctors.isEmpty()) {
                // 查询可用的诊室（is_free=1）
                LambdaQueryWrapper<RoomBusy> availableRoomWrapper = new LambdaQueryWrapper<RoomBusy>()
                    .eq(RoomBusy::getScheduleDate, LocalDate.parse(scheduleDate))
                    .eq(RoomBusy::getTimeSlot, roomBusyTimeSlot)
                    .eq(RoomBusy::getDepartmentId, departmentId)
                    .eq(RoomBusy::getHospitalId, hospitalId)
                    .eq(RoomBusy::getIsFree, true);
                
                List<RoomBusy> availableRooms = roomBusyService.list(availableRoomWrapper);
                
                // 为每个医生分配一个诊室
                for (int i = 0; i < doctors.size() && i < availableRooms.size(); i++) {
                    Map<String, Object> doctorData = doctors.get(i);
                    String doctorName = (String) doctorData.get("doctorName");
                    String employeeId = (String) doctorData.get("employeeId");
                    
                    RoomBusy room = availableRooms.get(i);
                    
                    // 更新room_busy表，设置医生信息
                    LambdaUpdateWrapper<RoomBusy> roomUpdate = new LambdaUpdateWrapper<RoomBusy>()
                        .eq(RoomBusy::getId, room.getId())
                        .set(RoomBusy::getEmployeeId, employeeId)
                        .set(RoomBusy::getIsFree, false); // 设置为占用
                    roomBusyService.update(roomUpdate);
                    
                    // 更新doctor_schedule表，设置诊室号
                    LambdaUpdateWrapper<DoctorSchedule> scheduleUpdate = new LambdaUpdateWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                        .eq(DoctorSchedule::getTimeSlot, doctorScheduleTimeSlot)
                        .eq(DoctorSchedule::getDepartmentId, departmentId)
                        .eq(DoctorSchedule::getHospitalId, hospitalId)
                        .eq(DoctorSchedule::getEmployeeId, employeeId)
                        .set(DoctorSchedule::getRoomNumber, room.getRoomNumber());
                    doctorScheduleService.update(scheduleUpdate);
                }
            }
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
    
    /**
     * 重置指定周的排班
     */
    @PostMapping("/reset-week")
    @ResponseBody
    public String resetWeek(@RequestBody Map<String, Object> requestData) {
        try {
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");
            Long departmentId = Long.valueOf(requestData.get("departmentId").toString());
            
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return "error:unauthorized";
            }
            
            // 获取当前用户的医院ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                return "error:user_not_found";
            }
            
            // 检查时间是否已过或当前周
            if (isDateInPast(startDate) || isCurrentWeek(startDate, endDate)) {
                return "error:invalid_time_range";
            }
            
            // 执行重置逻辑
            doctorScheduleService.resetWeekSchedules(startDate, endDate, departmentId, currentUser.getHospitalId());
            
            return "success";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
    
    /**
     * 复制上周排班
     */
    @PostMapping("/copy-previous-week")
    @ResponseBody
    public String copyPreviousWeek(@RequestBody Map<String, Object> requestData) {
        try {
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");
            Long departmentId = Long.valueOf(requestData.get("departmentId").toString());
            
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return "error:unauthorized";
            }
            
            // 获取当前用户的医院ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                return "error:user_not_found";
            }
            
            Long hospitalId = currentUser.getHospitalId();
            
            // 检查时间是否已过或当前周
            if (isDateInPast(startDate) || isCurrentWeek(startDate, endDate)) {
                return "error:invalid_time_range";
            }
            
            // 计算前一周的日期范围
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDate prevStart = start.minusWeeks(1);
            LocalDate prevEnd = end.minusWeeks(1);
            
            // 在room_busy表中，删除所选周的记录
            LambdaQueryWrapper<RoomBusy> roomBusyDeleteWrapper = new LambdaQueryWrapper<RoomBusy>()
                .ge(RoomBusy::getScheduleDate, startDate)
                .le(RoomBusy::getScheduleDate, endDate)
                .eq(RoomBusy::getDepartmentId, departmentId)
                .eq(RoomBusy::getHospitalId, hospitalId);
            roomBusyService.remove(roomBusyDeleteWrapper);
            
            // 在doctor_schedule表中，删除所选周的记录
            LambdaQueryWrapper<DoctorSchedule> doctorScheduleDeleteWrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .ge(DoctorSchedule::getScheduleDate, startDate)
                .le(DoctorSchedule::getScheduleDate, endDate)
                .eq(DoctorSchedule::getDepartmentId, departmentId)
                .eq(DoctorSchedule::getHospitalId, hospitalId);
            doctorScheduleService.remove(doctorScheduleDeleteWrapper);
            
            // 复制前一周的room_busy表记录到所选周
            List<RoomBusy> prevRoomBusyList = roomBusyService.list(new LambdaQueryWrapper<RoomBusy>()
                .ge(RoomBusy::getScheduleDate, prevStart.toString())
                .le(RoomBusy::getScheduleDate, prevEnd.toString())
                .eq(RoomBusy::getDepartmentId, departmentId)
                .eq(RoomBusy::getHospitalId, hospitalId));
            
            for (RoomBusy prevRoomBusy : prevRoomBusyList) {
                // 将前一周的记录日期直接加7天，复制到当前周
                LocalDate newDate = prevRoomBusy.getScheduleDate().plusWeeks(1);
                
                RoomBusy newRoomBusy = new RoomBusy();
                newRoomBusy.setRoomNumber(prevRoomBusy.getRoomNumber());
                newRoomBusy.setHospitalId(prevRoomBusy.getHospitalId());
                newRoomBusy.setDepartmentId(prevRoomBusy.getDepartmentId());
                newRoomBusy.setScheduleDate(newDate); // 日期加7天
                newRoomBusy.setTimeSlot(prevRoomBusy.getTimeSlot());
                newRoomBusy.setIsFree(prevRoomBusy.getIsFree());
                newRoomBusy.setEmployeeId(prevRoomBusy.getEmployeeId()); // 复制员工ID
                
                roomBusyService.save(newRoomBusy);
            }
            
            // 复制前一周的doctor_schedule表记录到所选周
            List<DoctorSchedule> prevDoctorScheduleList = doctorScheduleService.list(new LambdaQueryWrapper<DoctorSchedule>()
                .ge(DoctorSchedule::getScheduleDate, prevStart.toString())
                .le(DoctorSchedule::getScheduleDate, prevEnd.toString())
                .eq(DoctorSchedule::getDepartmentId, departmentId)
                .eq(DoctorSchedule::getHospitalId, hospitalId)
                .ne(DoctorSchedule::getScheduleStatus, "CANCELLED"));
            
            for (DoctorSchedule prevSchedule : prevDoctorScheduleList) {
                // 将前一周的记录日期直接加7天，复制到当前周
                LocalDate newDate = prevSchedule.getScheduleDate().plusWeeks(1);
                
                DoctorSchedule newSchedule = new DoctorSchedule();
                newSchedule.setDoctorName(prevSchedule.getDoctorName());
                newSchedule.setEmployeeId(prevSchedule.getEmployeeId());
                newSchedule.setScheduleDate(newDate); // 日期加7天
                newSchedule.setTimeSlot(prevSchedule.getTimeSlot());
                newSchedule.setRegistrationQuota(prevSchedule.getRegistrationQuota());
                newSchedule.setDepartmentId(prevSchedule.getDepartmentId());
                newSchedule.setHospitalId(prevSchedule.getHospitalId());
                newSchedule.setScheduleStatus(prevSchedule.getScheduleStatus());
                newSchedule.setRegistrationFee(prevSchedule.getRegistrationFee());
                newSchedule.setRoomNumber(prevSchedule.getRoomNumber());
                newSchedule.setRegisteredCount(0); // 将registered_count字段的值改为0
                
                doctorScheduleService.save(newSchedule);
            }
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
    
    /**
     * 检查日期是否在今天之前
     */
    private boolean isDateInPast(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查是否是当前周
     */
    private boolean isCurrentWeek(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDate now = LocalDate.now();
            
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取指定科室在指定日期范围内的房间占用情况数据
     */
    @GetMapping("/room-busy-data")
    @ResponseBody
    public Map<String, Map<String, Map<String, Object>>> getRoomBusyData(
            @RequestParam Long departmentId,
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        
        // 从Spring Security上下文中获取当前用户医院ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return new HashMap<>();
        }
        
        // 获取当前用户的医院ID
        String username = auth.getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return new HashMap<>();
        }
        
        // 查询指定日期范围和科室的房间占用情况
        List<RoomBusy> roomBusyList = roomBusyService.getRoomBusyByDateRange(
            startDate, endDate, departmentId, currentUser.getHospitalId());
        
        // 按日期和时段组织数据
        Map<String, Map<String, Map<String, Object>>> result = new HashMap<>();
        
        for (RoomBusy rb : roomBusyList) {
            String date = rb.getScheduleDate().toString();
            String timeSlot = rb.getTimeSlot();
            
            result.putIfAbsent(date, new HashMap<>());
            result.get(date).putIfAbsent(timeSlot, new HashMap<>());
            
            // 计算已排医生数和剩余诊室数
            Map<String, Object> stats = result.get(date).get(timeSlot);
            
            // 初始化计数
            if (!stats.containsKey("occupiedCount")) {
                stats.put("occupiedCount", 0);
            }
            if (!stats.containsKey("availableCount")) {
                stats.put("availableCount", 0);
            }
            
            // 根据isFree字段统计
            if (rb.getIsFree() != null) {
                if (!rb.getIsFree()) { // 已占用 (false表示占用)
                    int occupiedCount = (Integer) stats.get("occupiedCount");
                    stats.put("occupiedCount", occupiedCount + 1);
                } else { // 可用 (true表示可用)
                    int availableCount = (Integer) stats.get("availableCount");
                    stats.put("availableCount", availableCount + 1);
                }
            }
        }
        
        // 对于没有数据的日期和时段，从consulting_room表获取默认可用诊室数
        // 只返回统计信息，不自动插入数据库记录
        // 数据库记录的插入应该在实际保存排班时进行
        List<ConsultingRoom> rooms = consultingRoomService.getRoomsByDepartmentAndHospital(departmentId, currentUser.getHospitalId());
        
        // 遍历日期范围内的每一天
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateString = date.toString();
            
            // 如果没有这个日期的数据，创建一个空的映射
            result.putIfAbsent(dateString, new HashMap<>());
            
            // 对于上午和下午时段，检查是否已有数据
            String[] timeSlots = {"上午", "下午"}; // 使用room_busy表中定义的格式
            for (String timeSlot : timeSlots) {
                if (!result.get(dateString).containsKey(timeSlot)) {
                    // 如果没有数据，从consulting_room表获取所有可用诊室数
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("occupiedCount", 0);
                    stats.put("availableCount", rooms.size());
                    result.get(dateString).put(timeSlot, stats);
                    
                    // 不再自动插入到room_busy表中
                    // 数据库记录的插入应该在实际保存排班时进行
                    // 这样可以避免为整个日期范围内的所有日期和时段都创建空记录
                }
            }
        }
        
        return result;
    }
    
    /**
     * 清理指定日期和科室的room_busy表记录
     */
    @PostMapping("/clear-room-busy")
    @ResponseBody
    public String clearRoomBusy(@RequestBody Map<String, Object> requestData) {
        try {
            String scheduleDate = (String) requestData.get("scheduleDate");
            Long departmentId = Long.valueOf(requestData.get("departmentId").toString());
            
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return "error:unauthorized";
            }
            
            // 获取当前用户的医院ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                return "error:user_not_found";
            }
            
            Long hospitalId = currentUser.getHospitalId();
            
            // 更新room_busy表中指定日期和科室的记录，将employee_id设为空，is_free设为true
            LambdaUpdateWrapper<RoomBusy> updateWrapper = new LambdaUpdateWrapper<RoomBusy>()
                .eq(RoomBusy::getScheduleDate, LocalDate.parse(scheduleDate))
                .eq(RoomBusy::getDepartmentId, departmentId)
                .eq(RoomBusy::getHospitalId, hospitalId)
                .set(RoomBusy::getEmployeeId, null)
                .set(RoomBusy::getIsFree, true);
            
            roomBusyService.update(updateWrapper);
            
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
    
    /**
     * 获取科室可用诊室数量
     */
    @GetMapping("/available-rooms")
    @ResponseBody
    public Map<String, Object> getAvailableRoomCount(@RequestParam Long departmentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                result.put("success", false);
                result.put("message", "未认证用户");
                result.put("count", 0);
                return result;
            }
            
            // 获取当前用户的医院ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                result.put("count", 0);
                return result;
            }
            
            // 查询科室的正常状态诊室数量
            List<ConsultingRoom> rooms = consultingRoomService.getRoomsByDepartmentAndHospital(departmentId, currentUser.getHospitalId());
            
            result.put("success", true);
            result.put("count", rooms.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("count", 0);
        }
        
        return result;
    }
    
    /**
     * 单人排班页面
     */
    @GetMapping("/individual")
    public String individualSchedule(HttpSession session, Model model) {
        // 从Spring Security上下文中获取认证用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        
        // 获取所有诊室信息
        List<ConsultingRoom> rooms = consultingRoomService.list();
        
        model.addAttribute("rooms", rooms);
        return "schedule/individual-schedule";
    }
    
    /**
     * 导出排班表
     */
    @PostMapping("/export-schedule")
    @ResponseBody
    public ResponseEntity<Resource> exportSchedule(@RequestBody Map<String, Object> requestData) {
        try {
            String exportType = (String) requestData.get("exportType"); // week or month
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");
            
            // 从Spring Security上下文中获取当前用户医院ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 获取当前用户的医院ID和员工ID
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // 对于医生用户，获取科室信息
            Long departmentId = null;
            if ("DOCTOR".equals(currentUser.getUserType())) {
                // 首先尝试通过relatedId查询doctor表获取科室信息
                departmentId = getCurrentUserDepartmentId(currentUser);
                
                // 如果通过relatedId没有找到科室信息，则尝试通过employeeId查询
                if (departmentId == null) {
                    System.out.println("通过relatedId未找到医生信息，尝试通过employeeId查询");
                    // 通过employeeId查询doctor表
                    com.example.keshe1.entity.Doctor doctor = doctorService.getOne(new LambdaQueryWrapper<com.example.keshe1.entity.Doctor>()
                        .eq(com.example.keshe1.entity.Doctor::getEmployeeId, currentUser.getEmployeeId()));
                    
                    if (doctor != null) {
                        departmentId = doctor.getDepartmentId();
                        System.out.println("通过employeeId查询到医生科室ID: " + departmentId);
                    } else {
                        System.out.println("通过employeeId也未找到医生信息");
                    }
                }
                
                if (departmentId == null) {
                    System.out.println("无法获取当前用户的科室ID");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                // 如果不是医生用户，尝试直接从request获取departmentId
                Object deptIdObj = requestData.get("departmentId");
                if (deptIdObj != null) {
                    departmentId = Long.valueOf(deptIdObj.toString());
                }
                if (departmentId == null) {
                    System.out.println("非医生用户未提供有效的departmentId");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }
            
            // 生成Excel文件
            File excelFile = doctorScheduleService.exportScheduleToExcel(
                exportType, startDate, endDate, departmentId, currentUser.getHospitalId());
            
            if (excelFile == null || !excelFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            InputStreamResource resource = new InputStreamResource(new FileInputStream(excelFile));
            
            String filename = exportType.equals("week") ? "周排班表_" : "月排班表_";
            filename += new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + ".xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelFile.length())
                .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 获取当前用户（医生）的科室ID
    private Long getCurrentUserDepartmentId(User user) {
        // 通过user的relatedId查询doctor表获取科室ID
        if (user.getRelatedId() != null) {
            // 通过relatedId查询doctor表获取departmentId
            com.example.keshe1.entity.Doctor doctor = doctorService.getById(user.getRelatedId());
            return doctor != null ? doctor.getDepartmentId() : null;
        }
        return null;
    }
    
    /**
     * 获取当前登录医生的排班信息
     */
    @GetMapping("/doctor-schedule")
    @ResponseBody
    public Map<String, Object> getDoctorSchedule() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 开始获取医生排班信息 ===");
            
            // 从Spring Security上下文中获取当前用户信息
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                System.out.println("用户未认证或匿名用户");
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            String username = auth.getName();
            System.out.println("当前登录用户名: " + username);
            
            User currentUser = userService.findByUsername(username);
            if (currentUser == null) {
                currentUser = userService.findByPhone(username);
            }
            if (currentUser == null) {
                currentUser = userService.findByEmployeeId(username);
            }
            if (currentUser == null || !"DOCTOR".equals(currentUser.getUserType())) {
                System.out.println("用户类型错误或未找到用户: " + (currentUser != null ? currentUser.getUserType() : "null"));
                result.put("success", false);
                result.put("message", "用户类型错误");
                return result;
            }
            
            System.out.println("当前用户ID: " + currentUser.getId());
            System.out.println("当前用户员工ID: " + currentUser.getEmployeeId());
            System.out.println("当前用户医院ID: " + currentUser.getHospitalId());
            System.out.println("当前用户关联ID: " + currentUser.getRelatedId());
            
            // 首先尝试通过relatedId查询doctor表获取医生信息
            com.example.keshe1.entity.Doctor doctor = null;
            if (currentUser.getRelatedId() != null) {
                doctor = doctorService.getById(currentUser.getRelatedId());
                System.out.println("通过relatedId查询到医生: " + (doctor != null ? doctor.getId() : "null"));
                if (doctor != null) {
                    System.out.println("医生部门ID: " + doctor.getDepartmentId());
                }
            }
            
            // 如果通过relatedId没有找到医生信息，则尝试通过employeeId查询
            if (doctor == null) {
                System.out.println("通过relatedId未找到医生信息，尝试通过employeeId查询");
                // 通过employeeId查询doctor表
                doctor = doctorService.getOne(new LambdaQueryWrapper<com.example.keshe1.entity.Doctor>()
                    .eq(com.example.keshe1.entity.Doctor::getEmployeeId, currentUser.getEmployeeId()));
                
                if (doctor != null) {
                    System.out.println("通过employeeId查询到医生: " + doctor.getId());
                    System.out.println("医生部门ID: " + doctor.getDepartmentId());
                } else {
                    System.out.println("通过employeeId也未找到医生信息");
                }
            }
            
            if (doctor == null) {
                System.out.println("未找到医生信息");
                result.put("success", false);
                result.put("message", "未找到医生信息");
                return result;
            }
            
            // 查询该医生的所有排班信息
            List<DoctorSchedule> schedules = doctorScheduleService.getDoctorSchedules(
                currentUser.getEmployeeId(), 
                doctor.getDepartmentId(), 
                currentUser.getHospitalId()
            );
            
            System.out.println("查询到排班记录数量: " + (schedules != null ? schedules.size() : 0));
            if (schedules != null) {
                for (DoctorSchedule schedule : schedules) {
                    System.out.println("排班记录 - 日期: " + schedule.getScheduleDate() + ", 时段: " + schedule.getTimeSlot() + ", 诊室: " + schedule.getRoomNumber());
                }
            }
            
            result.put("success", true);
            result.put("schedules", schedules);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        System.out.println("=== 结束获取医生排班信息 ===");
        return result;
    }
}