-- 演示如何正确注册一个新的患者用户并保持数据一致性
-- 以下是一个事务性的注册流程示例

-- 开始事务
START TRANSACTION;

-- 1. 在User表中创建新患者用户
INSERT INTO user (username, password, phone, email, user_type, real_name, status, created_time, updated_time) 
VALUES ('patient4', '$2a$10$9tJhl8egc9569XqT9p6eUe3k0QpS9XqT9p6eUe3k0QpS9XqT9p6eUe', '13900139004', 'patient4@example.com', 'PATIENT', '患者4', 'ACTIVE', NOW(), NOW());

-- 获取刚插入的用户ID
SET @new_user_id = LAST_INSERT_ID();

-- 2. 在Patient表中创建对应的患者记录
INSERT INTO patient (user_id, gender, birth_date, id_card, address, created_time, updated_time) 
VALUES (@new_user_id, '男', '1995-08-15', '110101199508154321', '北京市丰台区某某街道4号', NOW(), NOW());

-- 提交事务
COMMIT;

-- 验证新注册的患者用户
SELECT u.id as user_id, u.username, u.real_name, u.phone, p.id as patient_id, p.gender, p.id_card
FROM user u 
JOIN patient p ON u.id = p.user_id 
WHERE u.username = 'patient4';