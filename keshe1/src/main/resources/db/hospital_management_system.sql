-- 医疗健康管理系统数据库脚本

-- 创建数据库
DROP DATABASE IF EXISTS hospital_management;
CREATE DATABASE hospital_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_management;

-- 1. 医院表
CREATE TABLE hospital (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医院ID',
    code VARCHAR(10) NOT NULL UNIQUE COMMENT '医院编码',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '医院名称',
    address VARCHAR(200) COMMENT '医院地址',
    phone VARCHAR(20) COMMENT '联系电话',
    description TEXT COMMENT '医院简介',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '医院表';

-- 2. 科室表
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    hospital_id BIGINT NOT NULL COMMENT '所属医院ID',
    name VARCHAR(50) NOT NULL COMMENT '科室名称',
    description TEXT COMMENT '科室描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (hospital_id) REFERENCES hospital(id)
) COMMENT '科室表';

-- 2. 医生表
CREATE TABLE doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    user_id BIGINT COMMENT '关联用户ID',
    hospital_id BIGINT NOT NULL COMMENT '所属医院ID',
    employee_id VARCHAR(9) UNIQUE COMMENT '工号（9位数字，前3位为医院编码，后6位为医生序号）',
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    gender ENUM('男', '女') NOT NULL COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    department_id BIGINT NOT NULL COMMENT '所属科室ID',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(100) COMMENT '专长',
    introduction TEXT COMMENT '医生简介',
    phone_bound BOOLEAN DEFAULT FALSE COMMENT '电话号码是否已绑定',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (hospital_id) REFERENCES hospital(id),
    FOREIGN KEY (department_id) REFERENCES department(id)
) COMMENT '医生表';

-- 3. 患者表
CREATE TABLE patient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '患者ID',
    user_id BIGINT COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    gender ENUM('男', '女') NOT NULL COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    id_card VARCHAR(18) UNIQUE COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(200) COMMENT '地址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    medical_history TEXT COMMENT '既往病史',
    allergy_history TEXT COMMENT '过敏史',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '患者表';

-- 4. 预约挂号表
CREATE TABLE appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot ENUM('上午', '下午', '晚上') NOT NULL COMMENT '时间段',
    appointment_time DATETIME NOT NULL COMMENT '预约具体时间',
    status ENUM('待确认', '已确认', '已完成', '已取消', '已拒绝') DEFAULT '待确认' COMMENT '预约状态',
    reason TEXT COMMENT '就诊原因',
    notes TEXT COMMENT '备注',
    reject_reason TEXT COMMENT '拒绝原因（仅在状态为"已拒绝"时有值）',
    reminder_enabled TINYINT DEFAULT 0 COMMENT '是否设置提醒（0-未设置，1-已设置）',
    reminder_time_offset INT COMMENT '提醒时间偏移（单位：分钟，负数表示提前提醒）',
    reminder_methods VARCHAR(100) COMMENT '提醒方式（如短信、邮件、站内信等）',
    last_reminder_time DATETIME COMMENT '最后提醒时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
) COMMENT '预约挂号表';

-- 5. 电子病历表
CREATE TABLE medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '病历ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    visit_date DATE NOT NULL COMMENT '就诊日期',
    chief_complaint TEXT COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_illness TEXT COMMENT '既往史',
    physical_examination TEXT COMMENT '体格检查',
    auxiliary_examination TEXT COMMENT '辅助检查',
    diagnosis TEXT COMMENT '诊断',
    treatment_plan TEXT COMMENT '治疗方案',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
) COMMENT '电子病历表';

-- 6. 诊断记录表
CREATE TABLE diagnosis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '诊断ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    record_id BIGINT COMMENT '病历ID',
    diagnosis_date DATE NOT NULL COMMENT '诊断日期',
    diagnosis_type VARCHAR(50) COMMENT '诊断类型',
    diagnosis_name VARCHAR(100) COMMENT '诊断名称',
    description TEXT COMMENT '诊断描述',
    status VARCHAR(50) COMMENT '诊断状态',
    remarks TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (record_id) REFERENCES medical_record(id)
) COMMENT '诊断记录表';

-- 7. 检查报告表
CREATE TABLE examination (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检查报告ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    record_id BIGINT COMMENT '病历ID',
    examination_date DATE NOT NULL COMMENT '检查日期',
    examination_type VARCHAR(50) COMMENT '检查类型',
    examination_item VARCHAR(100) COMMENT '检查项目',
    examination_result TEXT COMMENT '检查结果',
    description TEXT COMMENT '检查描述',
    status VARCHAR(50) COMMENT '检查状态',
    report_path VARCHAR(200) COMMENT '报告附件路径',
    remarks TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (record_id) REFERENCES medical_record(id)
) COMMENT '检查报告表';

-- 8. 用药记录表
CREATE TABLE medication (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用药记录ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    record_id BIGINT COMMENT '病历ID',
    medication_name VARCHAR(100) NOT NULL COMMENT '药品名称',
    medication_type VARCHAR(50) COMMENT '药品类型',
    dosage VARCHAR(100) COMMENT '用法用量',
    frequency VARCHAR(50) COMMENT '用药频次',
    days INT COMMENT '用药天数',
    prescription_date DATE NOT NULL COMMENT '开药日期',
    stop_date DATE COMMENT '停药日期',
    status VARCHAR(50) COMMENT '用药状态',
    instructions TEXT COMMENT '用药说明',
    precautions TEXT COMMENT '注意事项',
    remarks TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (record_id) REFERENCES medical_record(id)
) COMMENT '用药记录表';

-- 9. 药品表
CREATE TABLE medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '药品ID',
    name VARCHAR(100) NOT NULL COMMENT '药品名称',
    generic_name VARCHAR(100) COMMENT '通用名',
    dosage_form VARCHAR(50) COMMENT '剂型',
    specification VARCHAR(50) COMMENT '规格',
    manufacturer VARCHAR(100) COMMENT '生产厂家',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) COMMENT '单价',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '药品表';

-- 10. 健康档案表
CREATE TABLE health_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '健康档案ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    blood_type ENUM('A', 'B', 'AB', 'O', '未知') COMMENT '血型',
    bmi DECIMAL(5,2) COMMENT 'BMI指数',
    blood_pressure VARCHAR(20) COMMENT '血压',
    heart_rate INT COMMENT '心率',
    last_physical_date DATE COMMENT '最近体检日期',
    family_medical_history TEXT COMMENT '家族病史',
    lifestyle_habits TEXT COMMENT '生活习惯',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id)
) COMMENT '健康档案表';

-- 11. 体检报告表
CREATE TABLE physical_exam (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '体检报告ID',
    profile_id BIGINT NOT NULL COMMENT '健康档案ID',
    exam_date DATE NOT NULL COMMENT '体检日期',
    exam_organization VARCHAR(100) COMMENT '体检机构',
    exam_doctor VARCHAR(50) COMMENT '体检医生',
    exam_result TEXT COMMENT '体检结果',
    conclusion TEXT COMMENT '体检结论',
    recommendations TEXT COMMENT '健康建议',
    next_exam_date DATE COMMENT '下次体检建议日期',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (profile_id) REFERENCES health_profile(id)
) COMMENT '体检报告表';

-- 12. 健康指标表
CREATE TABLE health_indicator (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '健康指标ID',
    profile_id BIGINT NOT NULL COMMENT '健康档案ID',
    indicator_name VARCHAR(50) NOT NULL COMMENT '指标名称',
    indicator_value VARCHAR(50) NOT NULL COMMENT '指标值',
    unit VARCHAR(20) COMMENT '单位',
    normal_range VARCHAR(50) COMMENT '正常范围',
    measure_date DATE NOT NULL COMMENT '测量日期',
    notes TEXT COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (profile_id) REFERENCES health_profile(id)
) COMMENT '健康指标表';

-- 13. 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    hospital_id BIGINT COMMENT '所属医院ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    phone VARCHAR(20) UNIQUE COMMENT '电话号码（唯一）',
    employee_id VARCHAR(9) UNIQUE COMMENT '工号（9位数字，医生专用）',
    email VARCHAR(100) COMMENT '邮箱',
    user_type ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL COMMENT '用户类型',
    related_id BIGINT COMMENT '关联ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'INACTIVE' COMMENT '账户状态',
    created_by_admin BOOLEAN DEFAULT FALSE COMMENT '是否由管理员创建',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (hospital_id) REFERENCES hospital(id)
) COMMENT '用户表';

-- 插入初始数据

-- 插入医院数据
INSERT INTO hospital (code, name, address, phone, description) VALUES 
('001', '人民医院', '北京市朝阳区人民路1号', '010-12345678', '综合性三级甲等医院'),
('002', '协和医院', '北京市东城区协和路2号', '010-87654321', '知名三甲医院');

-- 插入科室数据
INSERT INTO department (hospital_id, name, description) VALUES 
(1, '内科', '负责诊治各种内科疾病'),
(1, '外科', '负责各类外科手术及治疗'),
(1, '妇产科', '负责妇女和产妇相关疾病'),
(1, '儿科', '负责儿童相关疾病的诊治'),
(1, '眼科', '负责眼部疾病的诊治'),
(1, '耳鼻喉科', '负责耳鼻喉相关疾病的诊治'),
(1, '皮肤科', '负责皮肤相关疾病的诊治'),
(1, '口腔科', '负责口腔及牙齿相关疾病的诊治'),
(2, '内科', '负责诊治各种内科疾病'),
(2, '外科', '负责各类外科手术及治疗'),
(2, '妇产科', '负责妇女和产妇相关疾病'),
(2, '儿科', '负责儿童相关疾病的诊治');

-- 插入医生数据
-- 注意：医生表的user_id字段需要与用户表的id字段关联
INSERT INTO doctor (hospital_id, user_id, employee_id, name, gender, phone, email, department_id, title, specialty, phone_bound) VALUES 
(1, 2, '001000001', '张伟', '男', '13800138001', 'zhangwei@example.com', 1, '主任医师', '心血管内科', TRUE),
(1, 3, '001000002', '李娜', '女', '13800138002', 'lina@example.com', 2, '副主任医师', '普外科', TRUE),
(1, 4, '001000003', '王强', '男', '13800138003', 'wangqiang@example.com', 3, '主治医师', '妇产科', TRUE),
(1, 5, '001000004', '赵敏', '女', '13800138004', 'zhaomin@example.com', 4, '主治医师', '儿科', TRUE),
(1, 6, '001000005', '刘洋', '男', '13800138005', 'liuyang@example.com', 5, '副主任医师', '眼科', TRUE),
(1, 7, '001000006', '陈丽', '女', '13800138006', 'chenli@example.com', 6, '主治医师', '耳鼻喉科', TRUE);

-- 插入患者数据
INSERT INTO patient (user_id, name, gender, birth_date, id_card, phone, email, address) VALUES 
(1, '杨小明', '男', '1990-05-15', '110101199005151234', '13900139001', 'yangxiaoming@example.com', '北京市朝阳区某某街道1号'),
(NULL, '黄小红', '女', '1985-12-20', '110101198512204321', 'huangxiaohong@example.com', '北京市海淀区某某街道2号'),
(NULL, '吴大宝', '男', '2015-03-10', '110101201503105678', 'wudabao@example.com', '北京市西城区某某街道3号');

-- 插入预约数据
INSERT INTO appointment (patient_id, doctor_id, appointment_date, time_slot, appointment_time, status, reason) VALUES 
(1, 1, '2023-12-25', '上午', '2023-12-25 09:00:00', '已确认', '胸闷气短'),
(2, 3, '2023-12-26', '下午', '2023-12-26 14:30:00', '待确认', '妇科检查'),
(3, 4, '2023-12-27', '上午', '2023-12-27 10:00:00', '已确认', '儿童感冒');

-- 插入电子病历数据
INSERT INTO medical_record (patient_id, doctor_id, visit_date, chief_complaint, present_illness, diagnosis, treatment_plan) VALUES 
(1, 1, '2023-12-20', '胸闷气短', '近一周感觉胸闷，偶有心悸', '高血压', '建议服药控制血压，定期复查'),
(1, 1, '2023-12-10', '咳嗽流涕', '上呼吸道感染症状', '上呼吸道感染', '建议休息，服用抗生素');

-- 插入诊断记录数据
INSERT INTO diagnosis (patient_id, doctor_id, record_id, diagnosis_date, diagnosis_type, diagnosis_name, description, status) VALUES 
(1, 1, 1, '2023-12-20', '临床诊断', '高血压', '原发性高血压，轻度', '确诊'),
(1, 1, 2, '2023-12-10', '临床诊断', '上呼吸道感染', '急性上呼吸道感染', '已治愈');

-- 插入检查报告数据
INSERT INTO examination (patient_id, doctor_id, record_id, examination_date, examination_type, examination_item, examination_result, status) VALUES 
(1, 1, 1, '2023-12-20', '血液检查', '血常规', '白细胞计数略高', '已完成'),
(2, 2, NULL, '2023-12-15', '影像学检查', '胸部X光', '肺部纹理增粗', '已完成');

-- 插入用药记录数据
INSERT INTO medication (patient_id, doctor_id, record_id, medication_name, medication_type, dosage, frequency, days, prescription_date, status, instructions) VALUES 
(1, 1, 1, '氨氯地平片', '西药', '5mg，每日一次', '每日一次', 30, '2023-12-20', '用药中', '餐前服用'),
(1, 1, 2, '阿莫西林胶囊', '西药', '0.5g，每日三次', '每日三次', 7, '2023-12-10', '已完成', '餐后服用');

-- 插入药品数据
INSERT INTO medicine (name, generic_name, dosage_form, specification, manufacturer, unit, price) VALUES 
('阿莫西林胶囊', '阿莫西林', '胶囊剂', '0.25g*24粒', '华北制药股份有限公司', '盒', 25.00),
('布洛芬片', '布洛芬', '片剂', '0.1g*20片', '中美天津史克制药有限公司', '盒', 18.50),
('头孢拉定颗粒', '头孢拉定', '颗粒剂', '0.125g*12袋', '哈药集团制药总厂', '盒', 32.00);

-- 插入默认用户数据
-- 密码为明文123456，系统启动时会自动加密
INSERT INTO user (hospital_id, username, password, phone, employee_id, email, user_type, related_id, real_name, status) VALUES 
(NULL, 'patient1', '123456', '13900139001', NULL, 'patient1@example.com', 'PATIENT', 1, '杨小明', 'ACTIVE'),
(1, '001000001', '123456', '13800138001', '001000001', 'zhangwei@example.com', 'DOCTOR', 1, '张伟', 'ACTIVE'),
(1, '001000002', '123456', '13800138002', '001000002', 'lina@example.com', 'DOCTOR', 2, '李娜', 'ACTIVE'),
(1, '001000003', '123456', '13800138003', '001000003', 'wangqiang@example.com', 'DOCTOR', 3, '王强', 'ACTIVE'),
(1, '001000004', '123456', '13800138004', '001000004', 'zhaomin@example.com', 'DOCTOR', 4, '赵敏', 'ACTIVE'),
(1, '001000005', '123456', '13800138005', '001000005', 'liuyang@example.com', 'DOCTOR', 5, '刘洋', 'ACTIVE'),
(1, '001000006', '123456', '13800138006', '001000006', 'chenli@example.com', 'DOCTOR', 6, '陈丽', 'ACTIVE'),
(1, 'admin', '123456', '13800138003', NULL, 'admin@example.com', 'ADMIN', NULL, '人民医院管理员', 'ACTIVE');

COMMIT;