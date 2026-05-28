package com.example.keshe1.config;

import com.example.keshe1.entity.*;
import com.example.keshe1.service.DepartmentService;
import com.example.keshe1.service.DiagnosisService;
import com.example.keshe1.service.DoctorService;
import com.example.keshe1.service.ExaminationService;
import com.example.keshe1.service.MedicationService;
import com.example.keshe1.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDate;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        // 首先修复数据库表结构，添加缺失的字段
        fixConsultingRoomTableStructure();
        
        // 检查是否有数据，如果没有则初始化
        if (departmentService.count() == 0) {
            initializeDepartments();
        }

        if (doctorService.count() == 0) {
            initializeDoctors();
        }

        if (patientService.count() == 0) {
            initializePatients();
        }

        // 只要相关Service存在，就可以初始化临床数据
        // examinationService 是否有数据，避免重复添加
        if (diagnosisService.count() == 0 && examinationService.count() == 0 && medicationService.count() == 0) {
            initializeClinicalData();
        }
    }

    private void fixConsultingRoomTableStructure() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            // 检查consulting_room表是否缺少created_time和updated_time列
            // 首先检查表是否存在
            int tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'consulting_room'", Integer.class);
            
            if (tableCount > 0) {
                // 检查created_time列是否存在
                int createdTimeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'consulting_room' AND column_name = 'created_time'", Integer.class);
                
                // 检查updated_time列是否存在
                int updatedTimeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'consulting_room' AND column_name = 'updated_time'", Integer.class);
                
                // 如果缺少任何一列，则添加它们
                if (createdTimeCount == 0 || updatedTimeCount == 0) {
                    String alterSql = "ALTER TABLE consulting_room " +
                        "ADD COLUMN IF NOT EXISTS created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                        "ADD COLUMN IF NOT EXISTS updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'";
                    jdbcTemplate.execute(alterSql);
                    System.out.println("已修复consulting_room表结构，添加了时间戳字段");
                } else {
                    System.out.println("consulting_room表结构已正确，无需修复");
                }
            } else {
                System.out.println("consulting_room表不存在");
            }
        } catch (Exception e) {
            System.err.println("修复consulting_room表结构时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDepartments() {
        Department dept1 = new Department();
        dept1.setName("内科");
        dept1.setDescription("负责诊治各种内科疾病");
        dept1.setHospitalId(1L); // 设置医院ID
        departmentService.save(dept1);

        Department dept2 = new Department();
        dept2.setName("外科");
        dept2.setDescription("负责各类外科手术及治疗");
        dept2.setHospitalId(1L); // 设置医院ID
        departmentService.save(dept2);

        Department dept3 = new Department();
        dept3.setName("妇产科");
        dept3.setDescription("负责妇女和产妇相关疾病");
        dept3.setHospitalId(1L); // 设置医院ID
        departmentService.save(dept3);

        Department dept4 = new Department();
        dept4.setName("儿科");
        dept4.setDescription("负责儿童相关疾病的诊治");
        dept4.setHospitalId(1L); // 设置医院ID
        departmentService.save(dept4);
    }

    private void initializeDoctors() {
        Doctor doctor1 = new Doctor();
        doctor1.setEmployeeId("001000001");
        doctor1.setGender("男");
        doctor1.setDepartmentId(1L);
        doctor1.setTitle("主任医师");
        doctor1.setSpecialty("心血管内科");
        doctor1.setPhoneBound(true);
        doctorService.save(doctor1);

        Doctor doctor2 = new Doctor();
        doctor2.setEmployeeId("001000002");
        doctor2.setGender("女");
        doctor2.setDepartmentId(2L);
        doctor2.setTitle("副主任医师");
        doctor2.setSpecialty("普外科");
        doctor2.setPhoneBound(true);
        doctorService.save(doctor2);
    }

    private void initializePatients() {
        Patient patient1 = new Patient();
        patient1.setGender("男");
        patient1.setBirthDate(LocalDate.of(1990, 5, 15));
        patient1.setIdCard("110101199005151234");
        patient1.setAddress("北京市朝阳区某某街道1号");
        patientService.save(patient1);

        Patient patient2 = new Patient();
        patient2.setGender("女");
        patient2.setBirthDate(LocalDate.of(1985, 12, 20));
        patient2.setIdCard("110101198512204321");
        patient2.setAddress("北京市海淀区某某街道2号");
        patientService.save(patient2);
    }

    private void initializeClinicalData() {
        // --- 1. 诊断记录 ---
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setPatientId(1L);
        diagnosis1.setDoctorId(1L);
        diagnosis1.setDiagnosisDate(LocalDate.now());
        diagnosis1.setDiagnosisType("临床诊断");
        diagnosis1.setDiagnosisName("高血压");
        diagnosis1.setDescription("原发性高血压，轻度");
        diagnosis1.setStatus("确诊");
        diagnosisService.save(diagnosis1);

        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setPatientId(1L);
        diagnosis2.setDoctorId(1L);
        diagnosis2.setDiagnosisDate(LocalDate.now().minusDays(10));
        diagnosis2.setDiagnosisType("临床诊断");
        diagnosis2.setDiagnosisName("上呼吸道感染");
        diagnosis2.setDescription("急性上呼吸道感染");
        diagnosis2.setStatus("已治愈");
        diagnosisService.save(diagnosis2);

        // --- 2. 检查报告 ---
        Examination examination1 = new Examination();
        examination1.setPatientId(1L);
        examination1.setDoctorId(1L);
        examination1.setExaminationDate(LocalDate.now());
        examination1.setExaminationItemId(1L);

        examination1.setExaminationResult("白细胞计数略高");
        examination1.setStatus("已完成");
        examinationService.save(examination1);

        Examination examination2 = new Examination();
        examination2.setPatientId(2L);
        examination2.setDoctorId(2L);
        examination2.setExaminationDate(LocalDate.now().minusDays(5));

        // 【修改点】: 以前是 setExaminationItem("胸部X光")
        // 假设数据库 examination_item 表里 ID=9 是 "胸部CT平扫" (替代X光)
        examination2.setExaminationItemId(9L);

        examination2.setExaminationResult("肺部纹理增粗");
        examination2.setStatus("已完成");
        examinationService.save(examination2);

        // --- 3. 用药记录 ---
        Medication medication1 = new Medication();
        medication1.setPatientId(1L);
        medication1.setDoctorId(1L);
        medication1.setMedicationName("氨氯地平片");
        medication1.setMedicationType("西药");
        medication1.setDosage("5mg，每日一次");
        medication1.setFrequency("每日一次");
        medication1.setDays(30);
        medication1.setPrescriptionDate(LocalDate.now());
        medication1.setStatus("用药中");
        medication1.setInstructions("餐前服用");
        medicationService.save(medication1);

        Medication medication2 = new Medication();
        medication2.setPatientId(1L);
        medication2.setDoctorId(1L);
        medication2.setMedicationName("阿莫西林胶囊");
        medication2.setMedicationType("西药");
        medication2.setDosage("0.5g，每日三次");
        medication2.setFrequency("每日三次");
        medication2.setDays(7);
        medication2.setPrescriptionDate(LocalDate.now().minusDays(10));
        medication2.setStatus("已完成");
        medication2.setInstructions("餐后服用");
        medicationService.save(medication2);
    }
}