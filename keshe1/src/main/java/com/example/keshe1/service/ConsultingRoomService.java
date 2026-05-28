package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.ConsultingRoom;

public interface ConsultingRoomService extends IService<ConsultingRoom> {
    
    /**
     * 根据科室和医院获取诊室列表
     */
    java.util.List<ConsultingRoom> getRoomsByDepartmentAndHospital(Long departmentId, Long hospitalId);
}