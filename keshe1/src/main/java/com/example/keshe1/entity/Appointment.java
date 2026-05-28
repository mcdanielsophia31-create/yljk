package com.example.keshe1.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@TableName("appointment")
public class Appointment extends BaseEntity {

    /**
     * 预约ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 医生ID
     */
    private Long doctorId;


    /**
     * 患者姓名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 标记为非数据库表字段
    private String patientName;

    /**
     * 患者年龄（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 标记为非数据库表字段
    private Integer patientAge;

    /**
     * 患者性别（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 标记为非数据库表字段
    private String patientGender;

    /**
     * 患者电话（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 标记为非数据库表字段
    private String patientPhone;

    /**
     * 患者身份证号（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 标记为非数据库表字段
    private String patientIdCard;

    /**
     * 医生姓名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 关键：标记为非数据库表字段
    private String doctorName;

    /**
     * 科室名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false) // 关键：标记为非数据库表字段
    private String departmentName;

    /**
     * 预约日期
     */
    private LocalDate appointmentDate;

    /**
     * 时间段（上午/下午/晚上）
     */
    private String timeSlot;

    /**
     * 预约具体时间
     */
    private LocalDateTime appointmentTime;

    /**
     * 预约状态（待确认/已确认/已取消/已完成/已拒绝）
     */
    private String status;

    /**
     * 就诊原因
     */
    private String reason;

    /**
     * 备注（既往病史等）
     */
    private String notes;

    /**
     * 拒绝原因（仅在状态为"已拒绝"时有值）
     */
    private String rejectReason;

    /**
     * 是否设置提醒（0-未设置，1-已设置）
     */
    private Integer reminderEnabled;

    /**
     * 提醒时间（相对于预约的时间偏移，单位：分钟，负数表示提前提醒）
     */
    private Integer reminderTimeOffset;

    /**
     * 提醒方式（如：短信、邮件、站内信等，以逗号分隔）
     */
    private String reminderMethods;

    /**
     * 最后提醒时间
     */
    private LocalDateTime lastReminderTime;

    /**
     * 提醒备注
     */
    @TableField("reminder_notes")  // 确保数据库表中有这个字段
    private String reminderNotes;

    /**
     * 提醒时间（具体的提醒时间点）
     */
    @TableField("reminder_time")  // 确保数据库表中有这个字段
    private LocalDateTime reminderTime;
    // 在 Appointment 实体类中添加以下字段



    // 添加 getter 和 setter


    // ========== 原有getter/setter ==========
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ========== 拒绝原因字段的getter/setter ==========
    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    // ========== 提醒相关字段的getter/setter ==========
    public Integer getReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(Integer reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public Integer getReminderTimeOffset() {
        return reminderTimeOffset;
    }

    public void setReminderTimeOffset(Integer reminderTimeOffset) {
        this.reminderTimeOffset = reminderTimeOffset;
    }

    public String getReminderMethods() {
        return reminderMethods;
    }

    public void setReminderMethods(String reminderMethods) {
        this.reminderMethods = reminderMethods;
    }

    public LocalDateTime getLastReminderTime() {
        return lastReminderTime;
    }

    public void setLastReminderTime(LocalDateTime lastReminderTime) {
        this.lastReminderTime = lastReminderTime;
    }

    // ========== 新增方法 ==========
    /**
     * 检查是否已设置提醒
     * @return true表示已设置提醒，false表示未设置
     */
    public boolean isReminderSet() {
        return this.reminderEnabled != null && this.reminderEnabled == 1;
    }

    // ========== 新增字段的getter/setter ==========
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientIdCard() {
        return patientIdCard;
    }

    public void setPatientIdCard(String patientIdCard) {
        this.patientIdCard = patientIdCard;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getReminderNotes() {
        return reminderNotes;
    }

    public void setReminderNotes(String reminderNotes) {
        this.reminderNotes = reminderNotes;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }
}