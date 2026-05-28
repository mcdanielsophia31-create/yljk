package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属医院ID
     */
    private Long hospitalId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 电话号码（唯一）
     */
    private String phone;

    /**
     * 工号（9位数字，医生专用）
     */
    private String employeeId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户类型（PATIENT: 病人, DOCTOR: 医生, ADMIN: 医院管理员）
     */
    private String userType;

    /**
     * 关联ID（根据用户类型关联到不同表的ID）
     * 如果是病人，关联patient表的id
     * 如果是医生，关联doctor表的id
     * 如果是管理员，此字段为空
     */
    private Long relatedId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 账户状态（ACTIVE: 激活, INACTIVE: 未激活, LOCKED: 锁定）
     */
    private String status;

    /**
     * 是否由管理员创建
     */
    private Boolean createdByAdmin;

    /**
     * 生日
     */
    private String birthDay;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    // 获取器和设置器（新增avatar的getter和setter）

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getCreatedByAdmin() {
        return createdByAdmin;
    }

    public void setCreatedByAdmin(Boolean createdByAdmin) {
        this.createdByAdmin = createdByAdmin;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}