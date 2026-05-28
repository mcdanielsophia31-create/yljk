package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.ConsultingRoom;
import com.example.keshe1.entity.RoomBusy;
import com.example.keshe1.mapper.RoomBusyMapper;
import com.example.keshe1.service.ConsultingRoomService;
import com.example.keshe1.service.RoomBusyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomBusyServiceImpl extends ServiceImpl<RoomBusyMapper, RoomBusy> implements RoomBusyService {
    
    @Autowired
    private ConsultingRoomService consultingRoomService;
    
    @Override
    public List<RoomBusy> getRoomBusyByDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId) {
        return this.list(new LambdaQueryWrapper<RoomBusy>()
            .eq(RoomBusy::getScheduleDate, scheduleDate)
            .eq(RoomBusy::getTimeSlot, timeSlot)
            .eq(RoomBusy::getDepartmentId, departmentId)
            .eq(RoomBusy::getHospitalId, hospitalId));
    }
    
    @Override
    public void initializeRoomBusyForDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId) {
        // 检查是否已存在数据
        List<RoomBusy> existing = this.list(new LambdaQueryWrapper<RoomBusy>()
            .eq(RoomBusy::getScheduleDate, scheduleDate)
            .eq(RoomBusy::getTimeSlot, timeSlot)
            .eq(RoomBusy::getDepartmentId, departmentId)
            .eq(RoomBusy::getHospitalId, hospitalId));
        
        if (!existing.isEmpty()) {
            // 如果已存在，直接返回
            return;
        }
        
        // 从ConsultingRoom表获取该科室的正常状态诊室
        List<ConsultingRoom> rooms = consultingRoomService.list(new LambdaQueryWrapper<ConsultingRoom>()
            .eq(ConsultingRoom::getDepartmentId, departmentId)
            .eq(ConsultingRoom::getHospitalId, hospitalId)
            .eq(ConsultingRoom::getStatus, "NORMAL"));
        
        // 为每个诊室创建RoomBusy记录
        for (ConsultingRoom room : rooms) {
            RoomBusy roomBusy = new RoomBusy();
            roomBusy.setRoomNumber(room.getRoomNumber());
            roomBusy.setHospitalId(room.getHospitalId());
            roomBusy.setDepartmentId(room.getDepartmentId());
            roomBusy.setScheduleDate(LocalDate.parse(scheduleDate));
            roomBusy.setTimeSlot(timeSlot);
            roomBusy.setIsFree(true); // true表示空闲
            
            this.save(roomBusy);
        }
    }
    
    @Override
    public List<RoomBusy> getRoomBusyByDateRange(String startDate, String endDate, Long departmentId, Long hospitalId) {
        return this.list(new LambdaQueryWrapper<RoomBusy>()
            .ge(RoomBusy::getScheduleDate, startDate)
            .le(RoomBusy::getScheduleDate, endDate)
            .eq(RoomBusy::getDepartmentId, departmentId)
            .eq(RoomBusy::getHospitalId, hospitalId));
    }
}