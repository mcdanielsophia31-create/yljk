package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.DoctorSchedule;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface DoctorScheduleService extends IService<DoctorSchedule> {
    
    /**
     * 根据日期范围和科室获取排班信息
     */
    List<DoctorSchedule> getSchedulesByDateRangeAndDepartment(String startDate, String endDate, Long departmentId, Long hospitalId);
    
    /**
     * 更新指定日期和时段的排班
     */
    void updateSchedulesForDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId, List<Map<String, Object>> doctors);
    
    /**
     * 重置指定周的排班
     */
    void resetWeekSchedules(String startDate, String endDate, Long departmentId, Long hospitalId);
    
    /**
     * 复制上周排班
     */
    void copyPreviousWeekSchedules(String startDate, String endDate, Long departmentId, Long hospitalId);
    
    /**
     * 导出排班表到Excel
     */
    File exportScheduleToExcel(String exportType, String startDate, String endDate, Long departmentId, Long hospitalId);
    
    /**
     * 获取指定日期和时段的已排医生数量和剩余诊室数量
     */
    Map<String, Integer> getScheduleStats(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId);
    
    /**
     * 更新指定日期和时段的排班信息，包含完整的业务逻辑
     */
    void updateSchedulesWithBusinessLogic(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId, List<Map<String, Object>> doctors);
    
    /**
     * 获取指定医生的排班信息
     */
    List<DoctorSchedule> getDoctorSchedules(String employeeId, Long departmentId, Long hospitalId);
}