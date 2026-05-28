package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("doctor_schedule")
public class DoctorSchedule extends BaseEntity {
    private Long id;
    private String employeeId;
    private String doctorName;
    private String roomNumber;
    private LocalDate scheduleDate;
    private String timeSlot; // '上午','下午'
    private Integer registrationQuota;
    private Integer registeredCount;
    private Long departmentId;
    private Long hospitalId;
    private String scheduleStatus; // 'VALID','CANCELLED','COMPLETED'
    private BigDecimal registrationFee;
    private String cancelReason;
}