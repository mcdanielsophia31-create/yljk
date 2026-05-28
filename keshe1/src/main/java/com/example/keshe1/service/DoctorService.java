package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.dto.DoctorDTO;
import com.example.keshe1.dto.DoctorDashboardStatsDTO;
import com.example.keshe1.entity.Doctor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DoctorService extends IService<Doctor> {

    /**
     * 根据用户ID获取医生信息
     */
    Doctor getByUserId(Long userId);

    /**
     * 根据姓名搜索医生
     */
    List<Doctor> searchByName(String name);

    /**
     * 根据工号获取医生信息
     */
    Doctor getByEmployeeId(String employeeId);

    /**
     * 根据电话号码获取医生信息
     */
    Doctor getByPhone(String phone);

    /**
     * 根据医院ID获取医生列表
     */
    List<Doctor> getByHospitalId(Long hospitalId);

    /**
     * 验证医生工号是否符合医院编码规则
     * @param employeeId 医生工号
     * @param hospitalId 医院ID
     * @return 是否符合规则
     */
    boolean validateEmployeeId(String employeeId, Long hospitalId);

    /**
     * 更新医生基本信息
     * @param doctor 医生信息
     * @return 是否成功
     */
    boolean updateDoctorInfo(Doctor doctor);
    //hsy加
    List<DoctorDTO> getDoctorsWithUserInfoByHospitalId(Long hospitalId);
    List<DoctorDTO> getDoctorsWithUserInfoByHospitalIdAndName(Long hospitalId, String name);
    List<DoctorDTO> getDoctorsWithUserInfoByName(String name);
    List<DoctorDTO> getAllDoctorsWithUserInfo();

    /**
     * 获取医生工作台的仪表盘统计数据（包含年龄分布）
     * @param doctorId 医生ID
     * @return 统计数据 DTO
     */
    DoctorDashboardStatsDTO getDashboardStats(Long doctorId);
    List<Doctor> getDoctorsByDepartment(Long departmentId);

    /**
     * 获取医生排班信息
     * @param doctorId 医生ID
     * @return 排班列表，每个排班包含日期、时间段、是否可预约等信息
     */
    List<Map<String, Object>> getDoctorSchedules(Long doctorId);

    /**
     * 获取医生某天的诊室排班信息
     * @param doctorId 医生ID
     * @param date 日期
     * @return 诊室列表，包含诊室号、时间段等
     */
    List<Map<String, Object>> getDoctorRoomsByDate(Long doctorId, LocalDate date);
    /**
     * 获取医生排班信息
     */
    Map<String, Object> getDoctorSchedule(Long doctorId, LocalDate date, String timeSlot);

    /**
     * 增加已预约人数
     */
    boolean incrementRegisteredCount(Long doctorId, LocalDate date, String timeSlot);
}