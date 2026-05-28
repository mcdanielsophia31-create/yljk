-- 修复Patient表中user_id为NULL的记录
-- 创建缺失的用户记录并建立关联

-- 为Patient ID为2的患者创建对应的用户记录
INSERT INTO user (username, password, phone, email, user_type, real_name, status, created_by_admin) 
SELECT 
    CONCAT('patient', p.id) as username,
    '$2a$10$9tJhl8egc9569XqT9p6eUe3k0QpS9XqT9p6eUe3k0QpS9XqT9p6eUe' as password, -- 加密后的默认密码
    p.address as phone,
    CONCAT('patient', p.id, '@example.com') as email,
    'PATIENT' as user_type,
    CONCAT('患者', p.id) as real_name, -- 使用更短的名称
    'ACTIVE' as status,
    FALSE as created_by_admin
FROM patient p 
WHERE p.user_id IS NULL AND p.id = 2;

-- 为Patient ID为3的患者创建对应的用户记录
INSERT INTO user (username, password, phone, email, user_type, real_name, status, created_by_admin) 
SELECT 
    CONCAT('patient', p.id) as username,
    '$2a$10$9tJhl8egc9569XqT9p6eUe3k0QpS9XqT9p6eUe3k0QpS9XqT9p6eUe' as password, -- 加密后的默认密码
    p.address as phone,
    CONCAT('patient', p.id, '@example.com') as email,
    'PATIENT' as user_type,
    CONCAT('患者', p.id) as real_name, -- 使用更短的名称
    'ACTIVE' as status,
    FALSE as created_by_admin
FROM patient p 
WHERE p.user_id IS NULL AND p.id = 3;

-- 更新Patient表，将新创建的用户ID与Patient记录关联
UPDATE patient p 
JOIN user u ON CONCAT('patient', p.id) = u.username 
SET p.user_id = u.id 
WHERE p.user_id IS NULL;

-- 验证修复结果
SELECT u.id as user_id, u.username, u.user_type, p.id as patient_id, p.user_id as patient_user_id
FROM user u 
LEFT JOIN patient p ON u.id = p.user_id 
WHERE u.user_type = 'PATIENT';