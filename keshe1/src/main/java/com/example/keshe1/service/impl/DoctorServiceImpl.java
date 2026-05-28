package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.dto.DoctorDTO;
import com.example.keshe1.dto.DoctorDashboardStatsDTO;
import com.example.keshe1.entity.Doctor;
import com.example.keshe1.entity.Appointment;
import com.example.keshe1.entity.DoctorSchedule;
import com.example.keshe1.entity.MedicalRecord;
import com.example.keshe1.mapper.DoctorMapper;
import com.example.keshe1.mapper.DoctorScheduleMapper;
import com.example.keshe1.mapper.MedicalRecordMapper;
import com.example.keshe1.mapper.AppointmentMapper;
import com.example.keshe1.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Doctor getByUserId(Long userId) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return doctorMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Doctor> searchByName(String name) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        return doctorMapper.selectList(queryWrapper);
    }

    @Override
    public Doctor getByEmployeeId(String employeeId) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId);
        return doctorMapper.selectOne(queryWrapper);
    }

    @Override
    public Doctor getByPhone(String phone) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return doctorMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Doctor> getByHospitalId(Long hospitalId) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hospital_id", hospitalId);
        return doctorMapper.selectList(queryWrapper);
    }

    @Override
    public boolean validateEmployeeId(String employeeId, Long hospitalId) {
        if (employeeId == null || employeeId.length() != 9) {
            return false;
        }
        if (!employeeId.matches("\\d+")) {
            return false;
        }
        String hospitalCode = String.format("%03d", hospitalId);
        return employeeId.startsWith(hospitalCode);
    }

    @Override
    public boolean updateDoctorInfo(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) {
            return false;
        }
        Doctor existingDoctor = this.getById(doctor.getId());
        if (existingDoctor == null) {
            return false;
        }
        if (doctor.getTitle() != null) existingDoctor.setTitle(doctor.getTitle());
        if (doctor.getGender() != null) existingDoctor.setGender(doctor.getGender());
        if (doctor.getBirthDate() != null) existingDoctor.setBirthDate(doctor.getBirthDate());
        if (doctor.getSpecialty() != null) existingDoctor.setSpecialty(doctor.getSpecialty());
        if (doctor.getIntroduction() != null) existingDoctor.setIntroduction(doctor.getIntroduction());
        return this.updateById(existingDoctor);
    }

    @Override
    public List<DoctorDTO> getDoctorsWithUserInfoByHospitalId(Long hospitalId) {
        return doctorMapper.selectDoctorsWithUserInfoByHospitalId(hospitalId);
    }

    @Override
    public List<DoctorDTO> getDoctorsWithUserInfoByHospitalIdAndName(Long hospitalId, String name) {
        return doctorMapper.selectDoctorsWithUserInfoByHospitalIdAndName(hospitalId, name);
    }

    @Override
    public List<DoctorDTO> getDoctorsWithUserInfoByName(String name) {
        return doctorMapper.selectDoctorsWithUserInfoByName(name);
    }

    @Override
    public List<DoctorDTO> getAllDoctorsWithUserInfo() {
        return doctorMapper.selectAllDoctorsWithUserInfo();
    }

    @Override
    public DoctorDashboardStatsDTO getDashboardStats(Long doctorId) {
        DoctorDashboardStatsDTO stats = new DoctorDashboardStatsDTO();

        // 1. 获取今日挂号量
        QueryWrapper<Appointment> todayQw = new QueryWrapper<>();
        todayQw.eq("doctor_id", doctorId);
        todayQw.eq("appointment_date", LocalDate.now());
        Long todayCount = appointmentMapper.selectCount(todayQw);
        stats.setTodayAppointments(Math.toIntExact(todayCount));

        // 2. 获取昨日挂号量 (用于计算趋势)
        QueryWrapper<Appointment> yesterdayQw = new QueryWrapper<>();
        yesterdayQw.eq("doctor_id", doctorId);
        yesterdayQw.eq("appointment_date", LocalDate.now().minusDays(1));
        Long yesterdayCount = appointmentMapper.selectCount(yesterdayQw);

        // 3. 计算增长率 (趋势)
        if (yesterdayCount == 0) {
            // 特殊情况：如果昨天是0
            if (todayCount > 0) {
                // 昨天0 -> 今天有：算作 100% 增长
                stats.setAppointmentTrend("+100%");
                stats.setIsTrendPositive(true);
            } else {
                // 昨天0 -> 今天0：无变化
                stats.setAppointmentTrend("0%");
                stats.setIsTrendPositive(true);
            }
        } else {
            // 正常计算公式：(今天 - 昨天) / 昨天
            double diff = todayCount - yesterdayCount;
            double percent = (diff / yesterdayCount) * 100;

            // 格式化字符串，保留整数，例如 "+50%" 或 "-20%"
            String sign = percent >= 0 ? "+" : "";
            stats.setAppointmentTrend(String.format("%s%.0f%%", sign, percent));
            stats.setIsTrendPositive(percent >= 0);
        }

        // 1.2 待确诊患者
        QueryWrapper<Appointment> pendingQw = new QueryWrapper<>();
        pendingQw.eq("doctor_id", doctorId)
                .eq("status", "待确认");
        stats.setPendingAppointments(Math.toIntExact(appointmentMapper.selectCount(pendingQw)));

        // 1.3 本月接诊
        QueryWrapper<MedicalRecord> monthQw = new QueryWrapper<>();
        monthQw.eq("doctor_id", doctorId)
                .apply("DATE_FORMAT(visit_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')");
        stats.setMonthlyVisits(Math.toIntExact(medicalRecordMapper.selectCount(monthQw)));

        // 1.4 管理患者总数
        QueryWrapper<MedicalRecord> totalQw = new QueryWrapper<>();
        totalQw.eq("doctor_id", doctorId);
        stats.setTotalPatients(Math.toIntExact(medicalRecordMapper.selectCount(totalQw)));

        // 2. 年龄分布
        Map<String, Integer> ageMap = new LinkedHashMap<>();
        ageMap.put("0-18岁", 0);
        ageMap.put("19-35岁", 0);
        ageMap.put("36-50岁", 0);
        ageMap.put("51-65岁", 0);
        ageMap.put("65岁以上", 0);

        List<Map<String, Object>> rawData = medicalRecordMapper.getPatientAgeDistribution(doctorId);
        if (rawData != null) {
            for (Map<String, Object> record : rawData) {
                String group = (String) record.get("age_group");
                Number countNum = (Number) record.get("count");
                Integer count = countNum != null ? countNum.intValue() : 0;
                if (ageMap.containsKey(group)) {
                    ageMap.put(group, count);
                }
            }
        }
        stats.setAgeDistribution(ageMap);

        return stats;
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        // 使用MyBatis-Plus查询该科室下的所有医生
        return lambdaQuery()
                .eq(Doctor::getDepartmentId, departmentId)
                .list();
    }

    @Autowired
    private DoctorScheduleMapper doctorScheduleMapper;


    @Override
    public List<Map<String, Object>> getDoctorSchedules(Long doctorId) {
        List<Map<String, Object>> schedules = new ArrayList<>();

        // 先根据医生ID获取医生的employeeId
        Doctor doctor = this.getById(doctorId);
        if (doctor == null || doctor.getEmployeeId() == null) {
            return schedules;
        }

        // 获取医生的工号
        String employeeId = doctor.getEmployeeId();
        System.out.println("原始工号: " + employeeId);

        // 处理工号格式：如果工号是8位，可能需要在前面补0
        if (employeeId.length() == 8) {
            employeeId = "0" + employeeId;
            System.out.println("工号格式转换: " + doctor.getEmployeeId() + " -> " + employeeId);
        }

        // ============ 修改：查询未来3个月的排班 ============
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;  // 从今天开始
        LocalDate endDate = today.plusMonths(3);  // 未来3个月

        System.out.println("查询时间范围: " + startDate + " 到 " + endDate);

        // 查询医生排班表
        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId)
                .ge("schedule_date", startDate)  // 大于等于今天
                .le("schedule_date", endDate)     // 小于等于3个月后
                .orderByAsc("schedule_date", "time_slot");

        List<DoctorSchedule> scheduleList = doctorScheduleMapper.selectList(queryWrapper);

        System.out.println("查询到 " + scheduleList.size() + " 条排班记录");

        for (DoctorSchedule schedule : scheduleList) {
            Map<String, Object> scheduleMap = new HashMap<>();

            // 修复日期格式
            String dateStr = schedule.getScheduleDate().toString();
            if (dateStr.startsWith("120")) {
                dateStr = "2" + dateStr.substring(2);
            }

            scheduleMap.put("scheduleDate", dateStr);
            scheduleMap.put("timeSlot", schedule.getTimeSlot());

            // 判断是否可预约：剩余号源 > 0 且 排班状态为VALID
            boolean isFree = schedule.getRegistrationQuota() != null &&
                    schedule.getRegisteredCount() != null &&
                    schedule.getRegistrationQuota() > schedule.getRegisteredCount()
                    && "VALID".equals(schedule.getScheduleStatus());
            scheduleMap.put("isFree", isFree ? 1 : 0);

            System.out.println("添加排班: 日期=" + dateStr +
                    ", 时间段=" + schedule.getTimeSlot() +
                    ", 可预约=" + isFree);

            schedules.add(scheduleMap);
        }

        System.out.println("返回排班数量: " + schedules.size());

        return schedules;
    }


    @Override
    public List<Map<String, Object>> getDoctorRoomsByDate(Long doctorId, LocalDate date) {
        List<Map<String, Object>> rooms = new ArrayList<>();

        Doctor doctor = this.getById(doctorId);
        if (doctor == null || doctor.getEmployeeId() == null) {
            return rooms;
        }

        // 处理工号格式：统一为9位
        String employeeId = doctor.getEmployeeId();
        System.out.println("原始工号: " + employeeId);

        // 根据数据库中的工号格式调整
        if (employeeId.length() == 8) {
            employeeId = "0" + employeeId;  // 8位补0变9位
        } else if (employeeId.length() == 7) {
            employeeId = "00" + employeeId;  // 7位补两个0
        }

        System.out.println("处理后的工号: " + employeeId);
        System.out.println("查询日期: " + date);

        // 查询该医生在指定日期的排班
        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId)
                .eq("schedule_date", date)
                .eq("schedule_status", "VALID");

        List<DoctorSchedule> schedules = doctorScheduleMapper.selectList(queryWrapper);

        System.out.println("查询到 " + schedules.size() + " 条排班记录");

        for (DoctorSchedule schedule : schedules) {
            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("doctorId", doctorId);
            roomMap.put("roomNumber", schedule.getRoomNumber());
            roomMap.put("timeSlot", schedule.getTimeSlot());

            // ============ 关键修改：返回号源信息 ============
            roomMap.put("registration_quota", schedule.getRegistrationQuota());  // 总号数
            roomMap.put("registered_count", schedule.getRegisteredCount());      // 已预约数

            // 判断是否可预约
            boolean isAvailable = schedule.getRegistrationQuota() > schedule.getRegisteredCount();
            roomMap.put("isAvailable", isAvailable ? 1 : 0);
            // ============ 修改结束 ============

            rooms.add(roomMap);

            System.out.println("找到诊室: " + schedule.getRoomNumber() + ", 时间段: " + schedule.getTimeSlot() +
                    ", 总号: " + schedule.getRegistrationQuota() + ", 已约: " + schedule.getRegisteredCount());
        }

        return rooms;
    }
    @Override
    public Map<String, Object> getDoctorSchedule(Long doctorId, LocalDate date, String timeSlot) {
        Doctor doctor = this.getById(doctorId);
        if (doctor == null || doctor.getEmployeeId() == null) {
            return null;
        }

        // 处理工号格式
        String employeeId = doctor.getEmployeeId();
        if (employeeId.length() == 8) {
            employeeId = "0" + employeeId;
        } else if (employeeId.length() == 7) {
            employeeId = "00" + employeeId;
        }

        // 查询排班
        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId)
                .eq("schedule_date", date)
                .eq("time_slot", timeSlot)
                .eq("schedule_status", "VALID");

        DoctorSchedule schedule = doctorScheduleMapper.selectOne(queryWrapper);

        if (schedule == null) {
            return null;
        }

        // 转换为Map
        Map<String, Object> result = new HashMap<>();
        result.put("id", schedule.getId());
        result.put("doctorId", doctorId);
        result.put("scheduleDate", schedule.getScheduleDate());
        result.put("timeSlot", schedule.getTimeSlot());
        result.put("registration_quota", schedule.getRegistrationQuota());
        result.put("registered_count", schedule.getRegisteredCount());
        result.put("roomNumber", schedule.getRoomNumber());

        return result;
    }

    @Override
    @Transactional
    public boolean incrementRegisteredCount(Long doctorId, LocalDate date, String timeSlot) {
        Doctor doctor = this.getById(doctorId);
        if (doctor == null || doctor.getEmployeeId() == null) {
            return false;
        }

        // 处理工号格式
        String employeeId = doctor.getEmployeeId();
        if (employeeId.length() == 8) {
            employeeId = "0" + employeeId;
        } else if (employeeId.length() == 7) {
            employeeId = "00" + employeeId;
        }

        // 查询当前排班
        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", employeeId)
                .eq("schedule_date", date)
                .eq("time_slot", timeSlot);

        DoctorSchedule schedule = doctorScheduleMapper.selectOne(queryWrapper);

        if (schedule == null) {
            return false;
        }

        // 检查是否还有剩余号
        Integer quota = schedule.getRegistrationQuota();
        Integer registered = schedule.getRegisteredCount();

        if (registered == null) registered = 0;
        if (quota == null) quota = 0;

        if (registered >= quota) {
            return false; // 已约满
        }

        // 更新 registered_count + 1
        schedule.setRegisteredCount(registered + 1);
        int rows = doctorScheduleMapper.updateById(schedule);

        return rows > 0;
    }
}
