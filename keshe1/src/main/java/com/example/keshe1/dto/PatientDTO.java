package com.example.keshe1.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientDTO {
    private Long id;                // 患者ID
    private Long userId;            // 用户ID
    private String patientName;     // 患者姓名（来自user表real_name）
    private String gender;          // 性别
    private LocalDate birthDate;    // 出生日期
    private String idCard;          // 身份证号
    private String address;         // 地址
    private String emergencyContact;// 紧急联系人
    private String emergencyPhone;  // 紧急联系电话
    private String medicalHistory;  // 既往病史
    private String allergyHistory;  // 过敏史
    private String phone;           // 手机号（来自user表）
    private String email;           // 邮箱（来自user表）
    private String username;        // 用户名（来自user表）
    private Integer age;            // 年龄（计算得出）

    public Integer getAge() {
        if (this.birthDate != null) {
            return java.time.Period.between(this.birthDate, java.time.LocalDate.now()).getYears();
        }
        return null;
    }
}