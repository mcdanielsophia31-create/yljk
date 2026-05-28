package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("consulting_room")
public class ConsultingRoom extends BaseEntity {
    private Long id;
    private String roomNumber;
    private Long hospitalId;
    private Long departmentId;
    private String location;
    private String status; // 'NORMAL','MAINTENANCE','DISABLED'
}