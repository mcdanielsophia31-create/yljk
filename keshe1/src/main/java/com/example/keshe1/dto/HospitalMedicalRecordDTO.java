package com.example.keshe1.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HospitalMedicalRecordDTO {
    private Long recordId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDate visitDate;
    private String chiefComplaint;
    private String presentIllness;
    private String diagnosis;
    private String treatmentPlan;
    private LocalDateTime createdTime;
    private Long departmentId;
    private String departmentName;
}