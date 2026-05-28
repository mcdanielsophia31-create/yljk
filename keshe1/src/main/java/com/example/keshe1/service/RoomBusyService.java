package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.RoomBusy;

import java.util.List;

public interface RoomBusyService extends IService<RoomBusy> {
    
    /**
     * 根据日期、时段和科室获取诊室占用情况
     */
    List<RoomBusy> getRoomBusyByDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId);
    
    /**
     * 初始化指定日期和时段的诊室占用情况
     */
    void initializeRoomBusyForDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId);
    
    /**
     * 根据日期范围和科室获取诊室占用情况
     */
    List<RoomBusy> getRoomBusyByDateRange(String startDate, String endDate, Long departmentId, Long hospitalId);
}