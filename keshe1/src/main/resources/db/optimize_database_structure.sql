-- 医院管理系统数据库结构优化脚本
-- 优化user、doctor、patient表结构，消除冗余字段，确保数据一致性

-- 备份原始数据（可选）
-- CREATE TABLE user_backup AS SELECT * FROM user;
-- CREATE TABLE doctor_backup AS SELECT * FROM doctor;
-- CREATE TABLE patient_backup AS SELECT * FROM patient;

-- 1. 为doctor表添加外键约束并移除冗余字段
-- 首先更新现有数据，将doctor表中的name, phone, email同步到对应的user记录
UPDATE user u 
JOIN doctor d ON u.id = d.user_id 
SET 
    u.real_name = COALESCE(u.real_name, d.name),
    u.phone = COALESCE(u.phone, d.phone),
    u.email = COALESCE(u.email, d.email)
WHERE u.user_type = 'DOCTOR';

-- 2. 为patient表添加外键约束并移除冗余字段
-- 更新现有数据，将patient表中的name, phone, email同步到对应的user记录
UPDATE user u 
JOIN patient p ON u.id = p.user_id 
SET 
    u.real_name = COALESCE(u.real_name, p.name),
    u.phone = COALESCE(u.phone, p.phone),
    u.email = COALESCE(u.email, p.email)
WHERE u.user_type = 'PATIENT';

-- 3. 修改doctor表结构，移除冗余字段
-- 注意：由于MySQL不支持一次ALTER语句中同时DROP多个字段，需要分步执行
ALTER TABLE doctor DROP COLUMN name;
ALTER TABLE doctor DROP COLUMN phone;
ALTER TABLE doctor DROP COLUMN email;

-- 4. 修改patient表结构，移除冗余字段
ALTER TABLE patient DROP COLUMN name;
ALTER TABLE patient DROP COLUMN phone;
ALTER TABLE patient DROP COLUMN email;

-- 5. 确保user表中的real_name, phone, email字段不为NULL（设置默认值）
ALTER TABLE user MODIFY COLUMN real_name VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE user MODIFY COLUMN phone VARCHAR(20) UNIQUE;
ALTER TABLE user MODIFY COLUMN email VARCHAR(100);

-- 6. 添加外键约束以确保数据完整性
-- doctor表的user_id字段应引用user表
ALTER TABLE doctor ADD CONSTRAINT fk_doctor_user_id 
    FOREIGN KEY (user_id) REFERENCES user(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- patient表的user_id字段应引用user表
ALTER TABLE patient ADD CONSTRAINT fk_patient_user_id 
    FOREIGN KEY (user_id) REFERENCES user(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 7. 创建视图以便于查询用户完整信息
-- 医生完整信息视图
CREATE OR REPLACE VIEW doctor_full_info AS
SELECT 
    d.id AS doctor_id,
    d.hospital_id,
    d.employee_id,
    d.department_id,
    d.title,
    d.specialty,
    d.introduction,
    d.phone_bound,
    d.created_time,
    d.updated_time,
    u.id AS user_id,
    u.username,
    u.real_name,
    u.phone,
    u.email,
    u.status
FROM doctor d
LEFT JOIN user u ON d.user_id = u.id;

-- 患者完整信息视图
CREATE OR REPLACE VIEW patient_full_info AS
SELECT 
    p.id AS patient_id,
    p.id_card,
    p.address,
    p.emergency_contact,
    p.emergency_phone,
    p.medical_history,
    p.allergy_history,
    p.created_time,
    p.updated_time,
    u.id AS user_id,
    u.username,
    u.real_name,
    u.phone,
    u.email,
    u.status
FROM patient p
LEFT JOIN user u ON p.user_id = u.id;

-- 优化完成
COMMIT;