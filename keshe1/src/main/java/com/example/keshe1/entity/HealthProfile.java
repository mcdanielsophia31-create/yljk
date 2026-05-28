package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@TableName("health_profile")
public class HealthProfile extends BaseEntity {

    /**
     * 健康档案ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 身高(cm)
     */
    private Double height;

    /**
     * 体重(kg)
     */
    private Double weight;

    /**
     * 血型
     */
    private String bloodType;

    /**
     * BMI指数
     */
    private Double bmi;

    /**
     * 血压
     */
    private String bloodPressure;

    /**
     * 心率
     */
    private Integer heartRate;

    /**
     * 最近体检日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastPhysicalDate;

    /**
     * 家族病史
     */
    private String familyMedicalHistory;

    /**
     * 生活习惯
     */
    private String lifestyleHabits;
    /**
     * 用于展示病人姓名
     */


    /**
     * 既往病史
     */
    private String medicalHistory;

    /**
     * 过敏史
     */
    private String allergies;
    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    @TableField(exist = false)
    private String patientName;

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

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public LocalDate getLastPhysicalDate() {
        return lastPhysicalDate;
    }

    public void setLastPhysicalDate(LocalDate lastPhysicalDate) {
        this.lastPhysicalDate = lastPhysicalDate;
    }

    public String getFamilyMedicalHistory() {
        return familyMedicalHistory;
    }

    public void setFamilyMedicalHistory(String familyMedicalHistory) {
        this.familyMedicalHistory = familyMedicalHistory;
    }

    public String getLifestyleHabits() {
        return lifestyleHabits;
    }

    public void setLifestyleHabits(String lifestyleHabits) {
        this.lifestyleHabits = lifestyleHabits;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

}