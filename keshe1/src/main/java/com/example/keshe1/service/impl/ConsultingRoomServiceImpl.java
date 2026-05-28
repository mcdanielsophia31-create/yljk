package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.ConsultingRoom;
import com.example.keshe1.mapper.ConsultingRoomMapper;
import com.example.keshe1.service.ConsultingRoomService;
import org.springframework.stereotype.Service;

@Service
public class ConsultingRoomServiceImpl extends ServiceImpl<ConsultingRoomMapper, ConsultingRoom> implements ConsultingRoomService {
    
    @Override
    public java.util.List<ConsultingRoom> getRoomsByDepartmentAndHospital(Long departmentId, Long hospitalId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConsultingRoom> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ConsultingRoom::getDepartmentId, departmentId)
               .eq(ConsultingRoom::getHospitalId, hospitalId)
               .eq(ConsultingRoom::getStatus, "NORMAL"); // 只获取状态为NORMAL的诊室
        
        return this.list(wrapper);
    }
}