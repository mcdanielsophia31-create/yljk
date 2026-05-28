-- 为appointment表添加提醒相关字段

-- 添加拒绝原因字段
ALTER TABLE appointment ADD COLUMN reject_reason TEXT COMMENT '拒绝原因（仅在状态为"已拒绝"时有值）';

-- 添加提醒启用字段
ALTER TABLE appointment ADD COLUMN reminder_enabled TINYINT DEFAULT 0 COMMENT '是否设置提醒（0-未设置，1-已设置）';

-- 添加提醒时间偏移字段
ALTER TABLE appointment ADD COLUMN reminder_time_offset INT COMMENT '提醒时间偏移（单位：分钟，负数表示提前提醒）';

-- 添加提醒方式字段
ALTER TABLE appointment ADD COLUMN reminder_methods VARCHAR(100) COMMENT '提醒方式（如短信、邮件、站内信等）';

-- 添加最后提醒时间字段
ALTER TABLE appointment ADD COLUMN last_reminder_time DATETIME COMMENT '最后提醒时间';

-- 更新time_slot枚举值，添加'晚上'
ALTER TABLE appointment MODIFY COLUMN time_slot ENUM('上午', '下午', '晚上') NOT NULL COMMENT '时间段';

-- 更新status枚举值，添加'已拒绝'
ALTER TABLE appointment MODIFY COLUMN status ENUM('待确认', '已确认', '已完成', '已取消', '已拒绝') DEFAULT '待确认' COMMENT '预约状态';

-- 为appointment表添加索引以提高查询性能
CREATE INDEX idx_appointment_patient_id ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor_id ON appointment(doctor_id);
CREATE INDEX idx_appointment_date ON appointment(appointment_date);