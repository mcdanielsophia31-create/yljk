package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("room_busy")
public class RoomBusy extends BaseEntity {
    private Long id;
    private String roomNumber;
    private Long hospitalId;
    private Long departmentId;
    private LocalDate scheduleDate;
    private String timeSlot; // '上午','下午'
    private Boolean isFree;
    private String employeeId;
    
    // 显式忽略BaseEntity中的审计字段，因为room_busy表中不存在这些字段
    @TableField(exist = false)
    private LocalDateTime createdTime;
    
    @TableField(exist = false)
    private LocalDateTime updatedTime;
}