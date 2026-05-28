-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: hospital_management
-- ------------------------------------------------------
-- Server version	8.0.36

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `time_slot` enum('上午','下午') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '时间段',
  `appointment_time` datetime NOT NULL COMMENT '预约具体时间',
  `status` enum('待确认','已确认','已拒绝','已完成','已取消') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '待确认' COMMENT '预约状态',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '就诊原因',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `reminder_enabled` tinyint(1) DEFAULT '0' COMMENT '是否开启提醒(0-否,1-是)',
  `reminder_time_offset` int DEFAULT '0' COMMENT '提醒时间偏移量(分钟，如提前30分钟提醒填30)',
  `reminder_methods` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '提醒方式，多个用逗号分隔(如：短信,微信,邮件)',
  `last_reminder_time` datetime DEFAULT NULL COMMENT '最后一次发送提醒的时间',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '医生拒绝预约的原因（仅已拒绝状态有值）',
  `reminder_time` datetime DEFAULT NULL,
  `reminder_notes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `patient_id` (`patient_id`) USING BTREE,
  KEY `doctor_id` (`doctor_id`) USING BTREE,
  CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='预约挂号表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (1,1,1,'2023-12-25','上午','2023-12-25 09:00:00','已确认','胸闷气短',NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22',0,0,'',NULL,NULL,NULL,NULL),(2,2,3,'2023-12-26','下午','2023-12-26 14:30:00','待确认','妇科检查',NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22',0,0,'',NULL,NULL,NULL,NULL),(3,3,4,'2023-12-27','上午','2023-12-27 10:00:00','已确认','儿童感冒',NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22',0,0,'',NULL,NULL,NULL,NULL),(4,1,1,'2025-12-31','上午','2025-12-23 19:42:55','已取消',NULL,NULL,NULL,NULL,0,0,'',NULL,NULL,NULL,NULL),(5,1,1,'2025-12-25','上午','2025-12-24 10:04:21','待确认',NULL,NULL,NULL,NULL,1,-120,'站内信',NULL,NULL,NULL,NULL),(6,1,2,'2025-12-26','上午','2025-12-24 10:22:16','待确认',NULL,NULL,NULL,NULL,0,0,'',NULL,NULL,NULL,NULL),(7,1,1,'2025-12-26','上午','2025-12-24 10:46:25','待确认','腿痛42\r\n','骨折',NULL,NULL,0,0,'',NULL,NULL,NULL,NULL),(8,1,3,'2025-12-26','下午','2025-12-24 14:48:56','已取消','肚子疼','无',NULL,NULL,0,-120,'站内信',NULL,NULL,NULL,NULL),(9,1,4,'2025-12-25','上午','2025-12-24 23:40:21','已取消',NULL,NULL,NULL,NULL,0,0,'',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consulting_room`
--

DROP TABLE IF EXISTS `consulting_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consulting_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_number` varchar(20) NOT NULL,
  `hospital_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  `location` varchar(100) NOT NULL,
  `status` enum('NORMAL','MAINTENANCE','DISABLED') NOT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consulting_room`
--

LOCK TABLES `consulting_room` WRITE;
/*!40000 ALTER TABLE `consulting_room` DISABLE KEYS */;
INSERT INTO `consulting_room` VALUES (1,'内科-01',1,1,'门诊楼2层201室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(73,'内科-02',1,1,'门诊楼2层202室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(74,'内科-03',1,1,'门诊楼2层203室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(75,'内科-04',1,1,'门诊楼2层204室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(76,'内科-05',1,1,'门诊楼2层205室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(77,'内科-06',1,1,'门诊楼2层206室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(78,'外科-01',1,2,'门诊楼3层301室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(79,'外科-02',1,2,'门诊楼3层302室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(80,'外科-03',1,2,'门诊楼3层303室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(81,'外科-04',1,2,'门诊楼3层304室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(82,'外科-05',1,2,'门诊楼3层305室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(83,'外科-06',1,2,'门诊楼3层306室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(84,'妇产科-01',1,3,'门诊楼4层401室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(85,'妇产科-02',1,3,'门诊楼4层402室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(86,'妇产科-03',1,3,'门诊楼4层403室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(87,'妇产科-04',1,3,'门诊楼4层404室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(88,'妇产科-05',1,3,'门诊楼4层405室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(89,'妇产科-06',1,3,'门诊楼4层406室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(90,'儿科-01',1,4,'门诊楼5层501室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(91,'儿科-02',1,4,'门诊楼5层502室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(92,'儿科-03',1,4,'门诊楼5层503室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(93,'儿科-04',1,4,'门诊楼5层504室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(94,'儿科-05',1,4,'门诊楼5层505室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(95,'儿科-06',1,4,'门诊楼5层506室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(96,'眼科-01',1,5,'门诊楼6层601室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(97,'眼科-02',1,5,'门诊楼6层602室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(98,'眼科-03',1,5,'门诊楼6层603室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(99,'眼科-04',1,5,'门诊楼6层604室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(100,'眼科-05',1,5,'门诊楼6层605室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(101,'眼科-06',1,5,'门诊楼6层606室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(102,'耳鼻喉科-01',1,6,'门诊楼7层701室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(103,'耳鼻喉科-02',1,6,'门诊楼7层702室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(104,'耳鼻喉科-03',1,6,'门诊楼7层703室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(105,'耳鼻喉科-04',1,6,'门诊楼7层704室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(106,'耳鼻喉科-05',1,6,'门诊楼7层705室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(107,'耳鼻喉科-06',1,6,'门诊楼7层706室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(108,'皮肤科-01',1,7,'门诊楼8层801室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(109,'皮肤科-02',1,7,'门诊楼8层802室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(110,'皮肤科-03',1,7,'门诊楼8层803室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(111,'皮肤科-04',1,7,'门诊楼8层804室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(112,'皮肤科-05',1,7,'门诊楼8层805室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(113,'皮肤科-06',1,7,'门诊楼8层806室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(114,'口腔科-01',1,8,'门诊楼9层901室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(115,'口腔科-02',1,8,'门诊楼9层902室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(116,'口腔科-03',1,8,'门诊楼9层903室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(117,'口腔科-04',1,8,'门诊楼9层904室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(118,'口腔科-05',1,8,'门诊楼9层905室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(119,'口腔科-06',1,8,'门诊楼9层906室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(120,'内科-01',2,9,'门诊楼2层201室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(121,'内科-02',2,9,'门诊楼2层202室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(122,'内科-03',2,9,'门诊楼2层203室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(123,'内科-04',2,9,'门诊楼2层204室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(124,'内科-05',2,9,'门诊楼2层205室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(125,'内科-06',2,9,'门诊楼2层206室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(126,'外科-01',2,10,'门诊楼3层301室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(127,'外科-02',2,10,'门诊楼3层302室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(128,'外科-03',2,10,'门诊楼3层303室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(129,'外科-04',2,10,'门诊楼3层304室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(130,'外科-05',2,10,'门诊楼3层305室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(131,'外科-06',2,10,'门诊楼3层306室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(132,'妇产科-01',2,11,'门诊楼4层401室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(133,'妇产科-02',2,11,'门诊楼4层402室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(134,'妇产科-03',2,11,'门诊楼4层403室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(135,'妇产科-04',2,11,'门诊楼4层404室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(136,'妇产科-05',2,11,'门诊楼4层405室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(137,'妇产科-06',2,11,'门诊楼4层406室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(138,'儿科-01',2,12,'门诊楼5层501室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(139,'儿科-02',2,12,'门诊楼5层502室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(140,'儿科-03',2,12,'门诊楼5层503室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(141,'儿科-04',2,12,'门诊楼5层504室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(142,'儿科-05',2,12,'门诊楼5层505室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59'),(143,'儿科-06',2,12,'门诊楼5层506室','NORMAL','2026-01-01 15:50:59','2026-01-01 15:50:59');
/*!40000 ALTER TABLE `consulting_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `hospital_id` bigint NOT NULL COMMENT '所属医院ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '科室名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '科室描述',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `room_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `hospital_id` (`hospital_id`) USING BTREE,
  CONSTRAINT `department_ibfk_1` FOREIGN KEY (`hospital_id`) REFERENCES `hospital` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='科室表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (1,1,'内科','负责诊治各种内科疾病','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(2,1,'外科','负责各类外科手术及治疗','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(3,1,'妇产科','负责妇女和产妇相关疾病','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(4,1,'儿科','负责儿童相关疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(5,1,'眼科','负责眼部疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(6,1,'耳鼻喉科','负责耳鼻喉相关疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(7,1,'皮肤科','负责皮肤相关疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(8,1,'口腔科','负责口腔及牙齿相关疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(9,2,'内科','负责诊治各种内科疾病','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(10,2,'外科','负责各类外科手术及治疗','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(11,2,'妇产科','负责妇女和产妇相关疾病','2025-12-23 14:18:22','2026-01-01 13:14:45',6),(12,2,'儿科','负责儿童相关疾病的诊治','2025-12-23 14:18:22','2026-01-01 13:14:45',6);
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnosis`
--

DROP TABLE IF EXISTS `diagnosis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diagnosis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '诊断ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `record_id` bigint DEFAULT NULL COMMENT '病历ID',
  `diagnosis_date` date NOT NULL COMMENT '诊断日期',
  `diagnosis_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '诊断类型',
  `diagnosis_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '诊断名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '诊断描述',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '诊断状态',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `patient_id` (`patient_id`) USING BTREE,
  KEY `doctor_id` (`doctor_id`) USING BTREE,
  KEY `record_id` (`record_id`) USING BTREE,
  CONSTRAINT `diagnosis_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `diagnosis_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `diagnosis_ibfk_3` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='诊断记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnosis`
--

LOCK TABLES `diagnosis` WRITE;
/*!40000 ALTER TABLE `diagnosis` DISABLE KEYS */;
INSERT INTO `diagnosis` VALUES (1,1,1,1,'2023-12-20','临床诊断','高血压','原发性高血压，轻度','确诊',NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22'),(2,1,1,2,'2023-12-10','临床诊断','上呼吸道感染','急性上呼吸道感染','已治愈',NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22');
/*!40000 ALTER TABLE `diagnosis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor`
--

DROP TABLE IF EXISTS `doctor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID',
  `hospital_id` bigint NOT NULL COMMENT '所属医院ID',
  `employee_id` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工号（9位数字，前3位为医院编码，后6位为医生序号）',
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `department_id` bigint NOT NULL COMMENT '所属科室ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称',
  `specialty` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专长',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '医生简介',
  `phone_bound` tinyint(1) DEFAULT '0' COMMENT '电话号码是否已绑定',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `registration_fee` decimal(10,2) DEFAULT '0.00' COMMENT '挂号费',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `employee_id` (`employee_id`) USING BTREE,
  KEY `hospital_id` (`hospital_id`) USING BTREE,
  KEY `department_id` (`department_id`) USING BTREE,
  KEY `fk_doctor_user_id` (`user_id`) USING BTREE,
  CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`hospital_id`) REFERENCES `hospital` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `doctor_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_doctor_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='医生表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor`
--

LOCK TABLES `doctor` WRITE;
/*!40000 ALTER TABLE `doctor` DISABLE KEYS */;
INSERT INTO `doctor` VALUES (1,2,1,'001000001','男','1987-01-15',1,'副主任医师','心血管内科','一个很棒的医生',1,'2025-12-23 14:18:22','2025-12-31 17:39:41',30.00),(2,4,1,'002000002','女',NULL,2,'副主任医师','普外科',NULL,1,'2025-12-23 14:18:22','2025-12-31 17:39:41',30.00),(3,11,1,'001000003','男',NULL,3,'主治医师','妇产科',NULL,1,'2025-12-23 14:18:22','2025-12-31 17:39:41',20.00),(4,12,1,'001000004','女',NULL,4,'主治医师','儿科',NULL,1,'2025-12-23 14:18:22','2025-12-31 17:39:41',20.00),(5,13,1,'001000005','男',NULL,5,'副主任医师','眼科',NULL,1,'2025-12-23 14:18:22','2025-12-31 17:39:41',30.00),(6,14,1,'001000006','女',NULL,6,'主治医师','耳鼻喉科',NULL,1,'2025-12-23 14:18:22','2026-01-01 13:20:34',20.00),(7,17,1,'00100007','男','1985-03-20',7,'主任医师','皮肤免疫学','擅长各类皮肤病的诊断与治疗，尤其精通过敏性皮肤病、自身免疫性皮肤病的诊疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',50.00),(8,18,1,'00100008','女','1988-07-12',8,'副主任医师','口腔修复学','专注口腔修复、牙体牙髓病治疗，擅长种植牙、烤瓷牙修复等技术',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',30.00),(9,19,1,'00100009','男','1990-05-08',1,'主治医师','呼吸内科','擅长慢性支气管炎、哮喘、肺炎等呼吸系统疾病的诊疗与康复指导',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',20.00),(10,20,1,'00100010','女','1992-09-15',1,'住院医师','消化内科','专攻胃炎、胃溃疡、肠炎等消化系统常见病的诊断与治疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',10.00),(11,21,1,'00100011','男','1986-11-22',1,'主任医师','心血管内科','擅长高血压、冠心病、心律失常等心血管疾病的精准诊疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',50.00),(12,22,1,'00100012','女','1989-04-30',1,'副主任医师','神经内科','专注头痛、头晕、脑血管疾病等神经内科疾病的诊治',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',30.00),(13,23,1,'00100013','男','1991-08-18',1,'主治医师','内分泌内科','擅长糖尿病、甲状腺疾病等内分泌代谢性疾病的治疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',20.00),(14,24,1,'00100014','女','1993-02-05',1,'住院医师','肾内科','专攻肾炎、肾病综合征等肾脏疾病的基础诊疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',10.00),(15,25,1,'00100015','男','1987-06-28',1,'主治医师','血液内科','擅长贫血、白血病等血液系统疾病的诊断与治疗',1,'2025-12-23 14:18:22','2026-01-01 13:20:34',20.00);
/*!40000 ALTER TABLE `doctor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `doctor_full_info`
--

DROP TABLE IF EXISTS `doctor_full_info`;
/*!50001 DROP VIEW IF EXISTS `doctor_full_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `doctor_full_info` AS SELECT 
 1 AS `doctor_id`,
 1 AS `hospital_id`,
 1 AS `employee_id`,
 1 AS `department_id`,
 1 AS `title`,
 1 AS `specialty`,
 1 AS `introduction`,
 1 AS `phone_bound`,
 1 AS `created_time`,
 1 AS `updated_time`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `real_name`,
 1 AS `phone`,
 1 AS `email`,
 1 AS `status`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `doctor_schedule`
--

DROP TABLE IF EXISTS `doctor_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` varchar(9) NOT NULL,
  `doctor_name` varchar(50) NOT NULL,
  `room_number` varchar(20) DEFAULT NULL,
  `schedule_date` date NOT NULL,
  `time_slot` enum('上午','下午') NOT NULL,
  `registration_quota` int NOT NULL DEFAULT '20',
  `registered_count` int NOT NULL DEFAULT '0',
  `department_id` bigint NOT NULL,
  `hospital_id` bigint NOT NULL,
  `schedule_status` enum('VALID','CANCELLED','COMPLETED') NOT NULL DEFAULT 'VALID',
  `registration_fee` decimal(10,2) NOT NULL DEFAULT '50.00',
  `cancel_reason` varchar(255) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_schedule`
--

LOCK TABLES `doctor_schedule` WRITE;
/*!40000 ALTER TABLE `doctor_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `doctor_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examination`
--

DROP TABLE IF EXISTS `examination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检查报告ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `record_id` bigint DEFAULT NULL COMMENT '病历ID',
  `examination_item_id` bigint DEFAULT NULL COMMENT '关联检查项目表ID',
  `examination_date` date NOT NULL COMMENT '检查日期',
  `examination_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '检查结果',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '检查描述',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '检查状态',
  `report_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '报告附件路径',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_record_item` (`record_id`,`examination_item_id`) USING BTREE,
  KEY `patient_id` (`patient_id`) USING BTREE,
  KEY `doctor_id` (`doctor_id`) USING BTREE,
  KEY `record_id` (`record_id`) USING BTREE,
  KEY `fk_examination_item_id` (`examination_item_id`) USING BTREE,
  CONSTRAINT `examination_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `examination_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `examination_ibfk_3` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_examination_item_id` FOREIGN KEY (`examination_item_id`) REFERENCES `examination_item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='检查报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examination`
--

LOCK TABLES `examination` WRITE;
/*!40000 ALTER TABLE `examination` DISABLE KEYS */;
INSERT INTO `examination` VALUES (3,1,1,1,1,'2023-12-20','数值超标',NULL,'已完成',NULL,NULL,NULL,NULL),(4,1,1,1,3,'2023-12-20','数值正常',NULL,'已完成',NULL,NULL,NULL,'2026-01-01 03:53:04'),(5,1,1,1,2,'2023-12-20','白细胞增多',NULL,'已完成',NULL,NULL,NULL,NULL),(7,1,1,1,14,'2023-12-20','一切功能正常。',NULL,'已完成',NULL,NULL,NULL,NULL),(14,1,1,2,14,'2023-12-10','1',NULL,'已完成',NULL,NULL,NULL,NULL),(15,2,1,5,1,'2026-01-01','病毒超标',NULL,'已完成',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `examination` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examination_item`
--

DROP TABLE IF EXISTS `examination_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检查项目ID',
  `examination_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检查类型',
  `examination_item` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检查项目',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '价格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='检查项目价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examination_item`
--

LOCK TABLES `examination_item` WRITE;
/*!40000 ALTER TABLE `examination_item` DISABLE KEYS */;
INSERT INTO `examination_item` VALUES (1,'临床检验','血常规',25.00),(2,'临床检验','尿常规',20.00),(3,'临床检验','肝功能',80.00),(4,'临床检验','肾功能',75.00),(5,'临床检验','血脂四项',60.00),(6,'临床检验','空腹血糖',15.00),(7,'临床检验','电解质分析',45.00),(8,'医学影像-CT','头颅CT平扫',350.00),(9,'医学影像-CT','胸部CT平扫',380.00),(10,'医学影像-CT','全腹CT平扫',450.00),(11,'医学影像-CT','颈椎CT平扫',360.00),(12,'医学影像-CT','腰椎CT平扫',360.00),(13,'医学影像-超声','腹部彩超',150.00),(14,'医学影像-超声','心脏彩超',220.00),(15,'医学影像-超声','甲状腺彩超',140.00),(16,'医学影像-超声','乳腺彩超',145.00),(17,'医学影像-超声','妇科彩超',155.00),(18,'内镜检查','无痛胃镜',680.00),(19,'内镜检查','无痛肠镜',780.00),(20,'内镜检查','普通胃镜',350.00),(21,'内镜检查','普通肠镜',420.00);
/*!40000 ALTER TABLE `examination_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_indicator`
--

DROP TABLE IF EXISTS `health_indicator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_indicator` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '健康指标ID',
  `profile_id` bigint NOT NULL COMMENT '健康档案ID',
  `indicator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标名称',
  `indicator_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指标值',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位',
  `normal_range` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '正常范围',
  `measure_date` date NOT NULL COMMENT '测量日期',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `profile_id` (`profile_id`) USING BTREE,
  CONSTRAINT `health_indicator_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `health_profile` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='健康指标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_indicator`
--

LOCK TABLES `health_indicator` WRITE;
/*!40000 ALTER TABLE `health_indicator` DISABLE KEYS */;
INSERT INTO `health_indicator` VALUES (49,25,'血压','145/92','mmHg','90-140/60-90','2025-01-15','一月：血压偏高（收缩压145mmHg，舒张压92mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(50,25,'心率','78','次/分','60-100','2025-01-15','一月：心率正常（78次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(51,26,'血压','142/91','mmHg','90-140/60-90','2025-02-12','二月：血压偏高（收缩压142mmHg，舒张压91mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(52,26,'心率','76','次/分','60-100','2025-02-12','二月：心率正常（76次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(53,27,'血压','148/93','mmHg','90-140/60-90','2025-03-18','三月：血压偏高（收缩压148mmHg，舒张压93mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(54,27,'心率','80','次/分','60-100','2025-03-18','三月：心率正常偏高（80次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(55,28,'血压','140/90','mmHg','90-140/60-90','2025-04-20','四月：血压正常偏高（收缩压140mmHg，舒张压90mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(56,28,'心率','75','次/分','60-100','2025-04-20','四月：心率正常（75次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(57,29,'血压','150/94','mmHg','90-140/60-90','2025-05-10','五月：血压偏高（收缩压150mmHg，舒张压94mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(58,29,'心率','82','次/分','60-100','2025-05-10','五月：心率正常偏高（82次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(59,30,'血压','146/92','mmHg','90-140/60-90','2025-06-08','六月：血压偏高（收缩压146mmHg，舒张压92mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(60,30,'心率','79','次/分','60-100','2025-06-08','六月：心率正常（79次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(61,31,'血压','152/95','mmHg','90-140/60-90','2025-07-16','七月：血压偏高（收缩压152mmHg，舒张压95mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(62,31,'心率','81','次/分','60-100','2025-07-16','七月：心率正常偏高（81次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(63,32,'血压','149/93','mmHg','90-140/60-90','2025-08-22','八月：血压偏高（收缩压149mmHg，舒张压93mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(64,32,'心率','83','次/分','60-100','2025-08-22','八月：心率正常偏高（83次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(65,33,'血压','155/96','mmHg','90-140/60-90','2025-09-14','九月：血压偏高（收缩压155mmHg，舒张压96mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(66,33,'心率','84','次/分','60-100','2025-09-14','九月：心率正常偏高（84次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(67,34,'血压','144/91','mmHg','90-140/60-90','2025-10-05','十月：血压偏高（收缩压144mmHg，舒张压91mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(68,34,'心率','77','次/分','60-100','2025-10-05','十月：心率正常（77次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(69,35,'血压','151/94','mmHg','90-140/60-90','2025-11-25','十一月：血压偏高（收缩压151mmHg，舒张压94mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(70,35,'心率','80','次/分','60-100','2025-11-25','十一月：心率正常偏高（80次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(71,36,'血压','147/92','mmHg','90-140/60-90','2025-12-19','十二月：血压偏高（收缩压147mmHg，舒张压92mmHg）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(72,36,'心率','79','次/分','60-100','2025-12-19','十二月：心率正常（79次/分）','2025-12-27 15:27:49','2025-12-27 15:27:49'),(73,37,'血压','118/78','mmHg','90-140/60-90','2025-01-10','一月：血压正常（收缩压118mmHg，舒张压78mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(74,37,'心率','72','次/分','60-100','2025-01-10','一月：心率正常（72次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(75,38,'血压','120/79','mmHg','90-140/60-90','2025-02-12','二月：血压正常（收缩压120mmHg，舒张压79mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(76,38,'心率','71','次/分','60-100','2025-02-12','二月：心率正常（71次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(77,39,'血压','122/80','mmHg','90-140/60-90','2025-03-15','三月：血压正常（收缩压122mmHg，舒张压80mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(78,39,'心率','73','次/分','60-100','2025-03-15','三月：心率正常（73次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(79,40,'血压','125/82','mmHg','90-140/60-90','2025-04-18','四月：血压正常（收缩压125mmHg，舒张压82mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(80,40,'心率','75','次/分','60-100','2025-04-18','四月：心率正常（75次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(81,41,'血压','128/83','mmHg','90-140/60-90','2025-05-20','五月：血压正常（收缩压128mmHg，舒张压83mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(82,41,'心率','76','次/分','60-100','2025-05-20','五月：心率正常（76次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(83,42,'血压','130/84','mmHg','90-140/60-90','2025-06-22','六月：血压正常偏高（收缩压130mmHg，舒张压84mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(84,42,'心率','77','次/分','60-100','2025-06-22','六月：心率正常（77次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(85,43,'血压','132/85','mmHg','90-140/60-90','2025-07-25','七月：血压正常偏高（收缩压132mmHg，舒张压85mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(86,43,'心率','78','次/分','60-100','2025-07-25','七月：心率正常（78次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(87,44,'血压','134/86','mmHg','90-140/60-90','2025-08-28','八月：血压正常偏高（收缩压134mmHg，舒张压86mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(88,44,'心率','79','次/分','60-100','2025-08-28','八月：心率正常（79次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(89,45,'血压','136/87','mmHg','90-140/60-90','2025-09-30','九月：血压正常偏高（收缩压136mmHg，舒张压87mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(90,45,'心率','80','次/分','60-100','2025-09-30','九月：心率正常偏高（80次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(91,46,'血压','138/88','mmHg','90-140/60-90','2025-10-05','十月：血压正常偏高（收缩压138mmHg，舒张压88mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(92,46,'心率','81','次/分','60-100','2025-10-05','十月：心率正常偏高（81次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(93,47,'血压','139/89','mmHg','90-140/60-90','2025-11-10','十一月：血压正常偏高（收缩压139mmHg，舒张压89mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(94,47,'心率','82','次/分','60-100','2025-11-10','十一月：心率正常偏高（82次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(95,48,'血压','135/85','mmHg','90-140/60-90','2025-12-15','十二月：血压正常偏高（收缩压135mmHg，舒张压85mmHg）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(96,48,'心率','80','次/分','60-100','2025-12-15','十二月：心率正常偏高（80次/分）','2025-12-27 15:29:01','2025-12-27 15:29:01'),(97,49,'血压','132/78','mmHg','90-140/60-90','2025-12-26','十二月：血压正常偏高（收缩压132mmHg，舒张压78mmHg）','2025-12-27 15:33:03','2025-12-27 15:33:03'),(98,49,'心率','79','次/分','60-100','2025-12-26','十二月：心率正常（79次/分）','2025-12-27 15:33:03','2025-12-27 15:33:03');
/*!40000 ALTER TABLE `health_indicator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_profile`
--

DROP TABLE IF EXISTS `health_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '健康档案ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `height` decimal(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `blood_type` enum('A','B','AB','O','未知') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '血型',
  `bmi` decimal(5,2) DEFAULT NULL COMMENT 'BMI指数',
  `blood_pressure` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '血压',
  `heart_rate` int DEFAULT NULL COMMENT '心率',
  `last_physical_date` date DEFAULT NULL COMMENT '最近体检日期',
  `family_medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '家族病史',
  `lifestyle_habits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '生活习惯',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_patient_time` (`patient_id`,`created_time` DESC) USING BTREE,
  CONSTRAINT `health_profile_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='健康档案表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_profile`
--

LOCK TABLES `health_profile` WRITE;
/*!40000 ALTER TABLE `health_profile` DISABLE KEYS */;
INSERT INTO `health_profile` VALUES (25,1,175.50,78.00,'A',25.30,'145/92',78,'2025-01-15','父亲有原发性高血压病史','偶尔熬夜，每周运动1次，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(26,1,175.50,78.20,'A',25.37,'142/91',76,'2025-02-12','父亲有原发性高血压病史','偶尔熬夜，每周运动1次，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(27,1,175.50,78.50,'A',25.47,'148/93',80,'2025-03-18','父亲有原发性高血压病史','经常熬夜，几乎不运动，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(28,1,175.50,78.80,'A',25.57,'140/90',75,'2025-04-20','父亲有原发性高血压病史','偶尔熬夜，每周运动1次，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(29,1,175.50,79.00,'A',25.64,'150/94',82,'2025-05-10','父亲有原发性高血压病史','经常熬夜，几乎不运动，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(30,1,175.50,79.20,'A',25.71,'146/92',79,'2025-06-08','父亲有原发性高血压病史','调整作息，每周运动2次，减少盐摄入','2025-12-27 15:27:49','2025-12-27 15:27:49'),(31,1,175.50,79.50,'A',25.81,'152/95',81,'2025-07-16','父亲有原发性高血压病史','调整作息，每周运动2次，减少盐摄入','2025-12-27 15:27:49','2025-12-27 15:27:49'),(32,1,175.50,79.80,'A',25.91,'149/93',83,'2025-08-22','父亲有原发性高血压病史','调整作息，每周运动2次，减少盐摄入','2025-12-27 15:27:49','2025-12-27 15:27:49'),(33,1,175.50,80.00,'A',25.98,'155/96',84,'2025-09-14','父亲有原发性高血压病史','偶尔熬夜，每周运动1次，口味偏咸','2025-12-27 15:27:49','2025-12-27 15:27:49'),(34,1,175.50,80.20,'A',26.05,'144/91',77,'2025-10-05','父亲有原发性高血压病史','遵医嘱服药，规律作息，每周运动3次','2025-12-27 15:27:49','2025-12-27 15:27:49'),(35,1,175.50,80.50,'A',26.15,'151/94',80,'2025-11-25','父亲有原发性高血压病史','遵医嘱服药，规律作息，每周运动3次','2025-12-27 15:27:49','2025-12-27 15:27:49'),(36,1,175.50,80.80,'A',26.25,'147/92',79,'2025-12-19','父亲有原发性高血压病史','遵医嘱服药，规律作息，每周运动3次','2025-12-27 15:27:49','2025-12-27 15:27:49'),(37,5,170.00,65.00,'O',22.49,'118/78',72,'2025-01-10','母亲有轻度高血压史','作息规律，每周运动2-3次，饮食清淡','2025-01-10 09:30:00','2025-12-27 15:36:19'),(38,5,170.00,65.20,'O',22.56,'120/79',71,'2025-02-12','母亲有轻度高血压史','作息规律，每周运动2-3次，饮食清淡','2025-02-12 10:15:00','2025-12-27 15:36:20'),(39,5,170.00,65.50,'O',22.66,'122/80',73,'2025-03-15','母亲有轻度高血压史','工作压力增大，运动减少','2025-03-15 11:00:00','2025-12-27 15:36:21'),(40,5,170.00,65.80,'O',22.77,'125/82',75,'2025-04-18','母亲有轻度高血压史','工作压力增大，运动减少','2025-04-18 14:20:00','2025-12-27 15:36:21'),(41,5,170.00,66.00,'O',22.84,'128/83',76,'2025-05-20','母亲有轻度高血压史','加班增多，睡眠不足','2025-05-20 15:30:00','2025-12-27 15:36:22'),(42,5,170.00,66.20,'O',22.91,'130/84',77,'2025-06-22','母亲有轻度高血压史','加班增多，睡眠不足','2025-06-22 09:45:00','2025-12-27 15:36:23'),(43,5,170.00,66.50,'O',23.02,'132/85',78,'2025-07-25','母亲有轻度高血压史','夏季饮食偏咸，饮水不足','2025-07-25 10:30:00','2025-12-27 15:36:23'),(44,5,170.00,66.80,'O',23.12,'134/86',79,'2025-08-28','母亲有轻度高血压史','夏季饮食偏咸，饮水不足','2025-08-28 11:15:00','2025-12-27 15:36:24'),(45,5,170.00,67.00,'O',23.20,'136/87',80,'2025-09-30','母亲有轻度高血压史','开始注意健康，减少加班','2025-09-30 13:00:00','2025-12-27 15:36:25'),(46,5,170.00,67.20,'O',23.27,'138/88',81,'2025-10-05','母亲有轻度高血压史','开始注意健康，减少加班','2025-10-05 14:45:00','2025-12-27 15:36:26'),(47,5,170.00,67.50,'O',23.38,'139/89',82,'2025-11-10','母亲有轻度高血压史','增加运动，改善饮食','2025-11-10 16:00:00','2025-12-27 15:36:29'),(48,5,170.00,67.80,'O',23.48,'135/85',80,'2025-12-15','母亲有轻度高血压史','规律作息，每周运动3次，低盐饮食','2025-12-15 08:30:00','2025-12-27 15:36:28'),(49,5,175.50,80.80,'A',26.25,'132/78',79,'2025-12-26','父亲有原发性高血压病史','遵医嘱服药，规律作息，每周运动3次','2025-12-27 15:33:03','2025-12-27 15:36:32');
/*!40000 ALTER TABLE `health_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospital`
--

DROP TABLE IF EXISTS `hospital`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hospital` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医院ID',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '医院编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '医院名称',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '医院地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '医院简介',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `code` (`code`) USING BTREE,
  UNIQUE KEY `name` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='医院表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospital`
--

LOCK TABLES `hospital` WRITE;
/*!40000 ALTER TABLE `hospital` DISABLE KEYS */;
INSERT INTO `hospital` VALUES (1,'001','人民医院','北京市朝阳区人民路1号','010-12345678','综合性三级甲等医院','2025-12-23 14:18:22','2025-12-23 14:18:22'),(2,'002','协和医院','北京市东城区协和路2号','010-87654321','知名三甲医院','2025-12-23 14:18:22','2025-12-23 14:18:22');
/*!40000 ALTER TABLE `hospital` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_record`
--

DROP TABLE IF EXISTS `medical_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '病历ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `chief_complaint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '主诉',
  `present_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '现病史',
  `past_illness` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '既往史',
  `physical_examination` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '体格检查',
  `auxiliary_examination` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '辅助检查',
  `diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '诊断',
  `treatment_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '治疗方案',
  `total_price` decimal(10,2) DEFAULT '0.00' COMMENT '总费用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `patient_id` (`patient_id`) USING BTREE,
  KEY `doctor_id` (`doctor_id`) USING BTREE,
  CONSTRAINT `medical_record_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `medical_record_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='电子病历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_record`
--

LOCK TABLES `medical_record` WRITE;
/*!40000 ALTER TABLE `medical_record` DISABLE KEYS */;
INSERT INTO `medical_record` VALUES (1,1,1,'2023-12-20','胸闷气短不适','近一周感觉胸闷，偶有心悸','','','血常规: 数值超标\n肝功能: 待完成\n尿常规: 白细胞增多\n\n\n','高血压','1. 阿莫西林胶囊 (阿莫西林)，规格：0.25g*24，用法：一天一次，每次1粒',0.00,'2025-12-23 14:18:22','2026-01-01 10:15:38'),(2,1,1,'2023-12-10','咳嗽流涕','上呼吸道感染症状','','','1. 心脏彩超: 1','上呼吸道感染','1. 头孢拉定颗粒 (头孢拉定)，规格：0.125g*12，用法：一天一次，每次1袋',0.00,'2025-12-23 14:18:22',NULL),(3,2,2,'2025-11-15','咳嗽、咳痰伴发热','患者3天前出现咳嗽、咳黄痰，伴发热，最高体温38.8℃，自行服用退烧药效果不佳','无','体温38.2℃，咽部充血，双肺呼吸音粗，可闻及湿性啰音。','','急性支气管炎','1. 阿莫西林胶囊 (阿莫西林)，规格：0.25g*24，用法：一天两次，每次1粒',0.00,'2025-12-29 09:48:49',NULL),(4,2,6,'2025-12-10','声音嘶哑、咽部异物感','患者1周前感冒后出现声音嘶哑，伴咽部干燥、异物感，说话费力，无发热','既往有过敏性鼻炎病史','体温36.5℃，咽部黏膜充血，声带充血、水肿，闭合不全','喉镜检查：声带充血水肿，活动正常\n血常规：正常范围','急性喉炎','',0.00,'2025-12-29 09:49:01','2025-12-30 14:29:53'),(5,2,1,'2026-01-01','肚子痛、腹泻、呕吐','患者一天前出现胃痛症状，腹泻、呕吐半日已久。','无','无','1. 血常规: 病毒超标','急性肠胃炎','[{\"medicineId\":1,\"medicineName\":\"阿莫西林胶囊\",\"genericName\":\"阿莫西林\",\"specification\":\"0.25g*24\",\"dosageUnit\":\"粒\",\"frequency\":\"一天两次\",\"quantity\":1,\"dosageForm\":\"胶囊剂\"},{\"medicineId\":2,\"medicineName\":\"布洛芬片\",\"genericName\":\"布洛芬\",\"specification\":\"0.1g*20\",\"dosageUnit\":\"粒\",\"frequency\":\"一天一次\",\"quantity\":1,\"dosageForm\":\"片剂\"}]',0.00,'2026-01-01 06:18:24','2026-01-01 09:55:36');
/*!40000 ALTER TABLE `medical_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medication`
--

DROP TABLE IF EXISTS `medication`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medication` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用药记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `record_id` bigint DEFAULT NULL COMMENT '病历ID',
  `medication_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '药品名称',
  `medication_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '药品类型',
  `dosage` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用法用量',
  `frequency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用药频次',
  `days` int DEFAULT NULL COMMENT '用药天数',
  `prescription_date` date NOT NULL COMMENT '开药日期',
  `stop_date` date DEFAULT NULL COMMENT '停药日期',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用药状态',
  `instructions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '用药说明',
  `precautions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '注意事项',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `patient_id` (`patient_id`) USING BTREE,
  KEY `doctor_id` (`doctor_id`) USING BTREE,
  KEY `record_id` (`record_id`) USING BTREE,
  CONSTRAINT `medication_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `medication_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `medication_ibfk_3` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用药记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medication`
--

LOCK TABLES `medication` WRITE;
/*!40000 ALTER TABLE `medication` DISABLE KEYS */;
INSERT INTO `medication` VALUES (1,1,1,1,'氨氯地平片','西药','5mg，每日一次','每日一次',30,'2023-12-20',NULL,'用药中','餐前服用',NULL,NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22'),(2,1,1,2,'阿莫西林胶囊','西药','0.5g，每日三次','每日三次',7,'2023-12-10',NULL,'已完成','餐后服用',NULL,NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22');
/*!40000 ALTER TABLE `medication` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medicine`
--

DROP TABLE IF EXISTS `medicine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicine` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '药品ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '药品名称',
  `generic_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通用名',
  `dosage_form` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '剂型',
  `specification` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格',
  `dosage_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用单位',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生产厂家',
  `stock_quantity` int NOT NULL COMMENT '库存数量',
  `stock_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '库存单位',
  `price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='药品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicine`
--

LOCK TABLES `medicine` WRITE;
/*!40000 ALTER TABLE `medicine` DISABLE KEYS */;
INSERT INTO `medicine` VALUES (1,'阿莫西林胶囊','阿莫西林','胶囊剂','0.25g*24','粒','华北制药股份有限公司',100,'盒',25.00,'2025-12-23 14:18:22','2025-12-30 13:44:47'),(2,'布洛芬片','布洛芬','片剂','0.1g*20','粒','中美天津史克制药有限公司',87,'盒',18.50,'2025-12-23 14:18:22','2025-12-30 13:45:51'),(3,'头孢拉定颗粒','头孢拉定','颗粒剂','0.125g*12','袋','哈药集团制药总厂',50,'盒',32.00,'2025-12-23 14:18:22','2025-12-30 13:45:59');
/*!40000 ALTER TABLE `medicine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID',
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系电话',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '既往病史',
  `allergy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '过敏史',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id_card` (`id_card`) USING BTREE,
  KEY `fk_patient_user_id` (`user_id`) USING BTREE,
  CONSTRAINT `fk_patient_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='患者表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient`
--

LOCK TABLES `patient` WRITE;
/*!40000 ALTER TABLE `patient` DISABLE KEYS */;
INSERT INTO `patient` VALUES (1,1,'男','1990-05-15','110101199005151234','北京市朝阳区某某街道1号',NULL,NULL,NULL,NULL,'2025-12-23 14:18:22','2025-12-23 18:43:27'),(2,5,'女','1985-12-20','110101198512204321','北京市海淀区某某街道2号',NULL,NULL,NULL,NULL,'2025-12-23 14:18:22','2025-12-25 13:43:59'),(3,6,'男','2015-03-10','110101201503105678','北京市西城区某某街道3号',NULL,NULL,NULL,NULL,'2025-12-23 14:18:22','2025-12-25 13:43:59'),(4,15,'女',NULL,'11111',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,16,'女',NULL,'1287236748',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `patient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `patient_full_info`
--

DROP TABLE IF EXISTS `patient_full_info`;
/*!50001 DROP VIEW IF EXISTS `patient_full_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `patient_full_info` AS SELECT 
 1 AS `patient_id`,
 1 AS `id_card`,
 1 AS `address`,
 1 AS `emergency_contact`,
 1 AS `emergency_phone`,
 1 AS `medical_history`,
 1 AS `allergy_history`,
 1 AS `created_time`,
 1 AS `updated_time`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `real_name`,
 1 AS `phone`,
 1 AS `email`,
 1 AS `status`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `physical_exam`
--

DROP TABLE IF EXISTS `physical_exam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `physical_exam` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '体检报告ID',
  `profile_id` bigint NOT NULL COMMENT '健康档案ID',
  `exam_date` date NOT NULL COMMENT '体检日期',
  `exam_organization` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '体检机构',
  `exam_doctor` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '体检医生',
  `exam_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '体检结果',
  `conclusion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '体检结论',
  `recommendations` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '健康建议',
  `next_exam_date` date DEFAULT NULL COMMENT '下次体检建议日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `profile_id` (`profile_id`) USING BTREE,
  CONSTRAINT `physical_exam_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `health_profile` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='体检报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `physical_exam`
--

LOCK TABLES `physical_exam` WRITE;
/*!40000 ALTER TABLE `physical_exam` DISABLE KEYS */;
/*!40000 ALTER TABLE `physical_exam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_busy`
--

DROP TABLE IF EXISTS `room_busy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_busy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_number` varchar(20) NOT NULL,
  `hospital_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  `schedule_date` date NOT NULL,
  `time_slot` enum('上午','下午') NOT NULL,
  `is_free` tinyint(1) NOT NULL DEFAULT '1',
  `employee_id` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_busy`
--

LOCK TABLES `room_busy` WRITE;
/*!40000 ALTER TABLE `room_busy` DISABLE KEYS */;
/*!40000 ALTER TABLE `room_busy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `hospital_id` bigint DEFAULT NULL COMMENT '所属医院ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `employee_id` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工号（9位数字，医生专用）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_type` enum('PATIENT','DOCTOR','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户类型',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `status` enum('ACTIVE','INACTIVE','LOCKED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'INACTIVE' COMMENT '账户状态',
  `created_by_admin` tinyint(1) DEFAULT '0' COMMENT '是否由管理员创建',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `birth_day` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  UNIQUE KEY `phone` (`phone`) USING BTREE,
  UNIQUE KEY `employee_id` (`employee_id`) USING BTREE,
  UNIQUE KEY `phone_2` (`phone`) USING BTREE,
  KEY `hospital_id` (`hospital_id`) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`hospital_id`) REFERENCES `hospital` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,NULL,'patient1','$2a$10$uwadTDJF3dJ5CBJDZU6O/.HRZ.5c.vuVqqOzHnL3Qc9a7BAsr2G2C','13900139001',NULL,'patient1@example.com','PATIENT',NULL,'杨小明','ACTIVE',0,NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22','2025-12-25','/uploads/avatars/patient_1/bf15d6c2-dbfe-4fc4-9fe0-132c5f8d7392.png'),(2,1,'001000001','$2a$10$YF9g81b98.mCdO4LmaTbKu.Jybpu8nyoJYgEWHDKDVAApDY92tpCC','13800138001','001000001','zhangwei@example.com','DOCTOR',NULL,'张伟','ACTIVE',0,NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22',NULL,'/uploads/avatars/doctor_2/doctor_avatar_2_1767072068253.jpg'),(3,1,'admin','$2a$10$WDZJ7eBkJoReTJksXtjgxe0Mw3m9sZHnSBYw.8RB34Os8m3bXJw.O','13800138003',NULL,'admin@example.com','ADMIN',NULL,'人民医院管理员','ACTIVE',0,NULL,'2025-12-23 14:18:22','2025-12-23 14:18:22',NULL,NULL),(4,2,'002000002','$2a$10$WDZJ7eBkJoReTJksXtjgxe0Mw3m9sZHnSBYw.8RB34Os8m3bXJw.O','13800138004','002000002','lina@example.com','DOCTOR',NULL,'中心医院医生','ACTIVE',0,NULL,'2025-12-24 10:27:01','2025-12-25 14:21:04',NULL,NULL),(5,NULL,'patient2','$2a$10$zivXC9ggyS3Ea3cNnU7SxO4KfzaBS5luX3IWKJT/j7Kh3eteqiMqW','13900139002',NULL,NULL,'PATIENT',NULL,'患者2','ACTIVE',0,NULL,'2025-12-25 13:43:04','2025-12-25 13:43:04',NULL,NULL),(6,NULL,'patient3','$2a$10$5Dwz0mfQMdJ.HkaynAMAW.PZI7JAA6Bwd7EPecs47h4rVCZfKMfna','13900139003',NULL,NULL,'PATIENT',NULL,'患者3','ACTIVE',0,NULL,'2025-12-25 13:43:04','2025-12-25 14:38:59',NULL,NULL),(11,1,'001000003','$2a$10$G77MhkOqR.DJ0/7SpE2RdubPc1ch0S4q0jQL2PCRtfQWIJn/apomy','13800138007','001000003','doctor3@example.com','DOCTOR',NULL,'医生3','ACTIVE',0,NULL,'2025-12-25 13:52:45','2026-01-01 13:21:05',NULL,NULL),(12,1,'001000004','$2a$10$encKx9RykgH.R2DKsrz6f.ykoR97TrwYSpDNrNCKnnj4B/FviM6rm','13800138008','001000004','doctor4@example.com','DOCTOR',NULL,'医生4','ACTIVE',0,NULL,'2025-12-25 13:52:45','2026-01-01 13:21:05',NULL,NULL),(13,1,'001000005','$2a$10$Qp/fX.Kwk4zflWVAY/JFfuPeRzU.IVeaMx43Dqpm.tamcFvL20/Ky','13800138009','001000005','doctor5@example.com','DOCTOR',NULL,'医生5','ACTIVE',0,NULL,'2025-12-25 13:52:45','2026-01-01 13:21:05',NULL,NULL),(14,1,'001000006','$2a$10$XgbFHzGczXduYnrns479E.NBu0QsPYBm2xTtee4jrxHyRy/Obixd2','13800138010','001000006','doctor6@example.com','DOCTOR',NULL,'医生6','ACTIVE',0,NULL,'2025-12-25 13:52:45','2026-01-01 13:21:05',NULL,NULL),(15,NULL,'wsx','$2a$10$GRK2/hIq4lJEGZW9UjEt7Ox7BSa.KnZecyyCA0ej1W4.VT0huMIAm','17875065787',NULL,'www@qq.com','PATIENT',NULL,'wsx','INACTIVE',0,NULL,NULL,NULL,NULL,NULL),(16,NULL,'patient4','$2a$10$0rgEev2oqPrunnw9qmvmWuI5T1FprNreo4SGjbIQDeIVKXc3Kyej.','1234567',NULL,'123333@qq.com','PATIENT',NULL,'测试','INACTIVE',0,NULL,NULL,NULL,NULL,NULL),(17,1,'001000007','$2a$10$VRx9mp61oItGOFY8koReiOzNls18hAGinfEXgK7SiPOUqBdmi8lzG','13800138011',NULL,'123456789@qq.com','DOCTOR',NULL,'医生7','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(18,1,'001000008','$2a$10$d42SNweOaE4knZ5MFsEDl.ElQ3SY3PTejrYfdmTEkGvtQ7mggiJEK','13800138012',NULL,'123456789@qq.com','DOCTOR',NULL,'医生8','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(19,1,'001000009','$2a$10$u70LYVuv13zKH.VEl7/WAOi.JtnhWJzUxZZf4RTAcQ1Z4S7t8PLuO','13800138013',NULL,'123456789@qq.com','DOCTOR',NULL,'医生9','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(20,1,'001000010','$2a$10$12LoPNR6lN0O4TiDuK7YCOjlgLcx.XHAJw9VxxVrG/M/d/jmFcoDO','13800138014',NULL,'123456789@qq.com','DOCTOR',NULL,'医生10','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(21,1,'001000011','$2a$10$6H78nItz/V4.XE64YzQ2pueYuBL14ZdnEUApPPtVCIi1WASqqPFEu','13800138015',NULL,'123456789@qq.com','DOCTOR',NULL,'医生11','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(22,1,'001000012','$2a$10$ltTAdXJIodWJ5Jg7bfq2Je/LOAcDcXcsDQch2iykpocFqOsBjpxvK','13800138016',NULL,'123456789@qq.com','DOCTOR',NULL,'医生12','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(23,1,'001000013','$2a$10$mjkb4iCApDuYy4YBZ7ArIuOktOr9MuwQnkL8BWMU8P/U4bMpG7EWC','13800138017',NULL,'123456789@qq.com','DOCTOR',NULL,'医生13','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(24,1,'001000014','$2a$10$7BCgdEpbO0XvThWDcMNnoOXEJj0dFZhlatKGHJthGsxNQ2o/Femwq','13800138018',NULL,'123456789@qq.com','DOCTOR',NULL,'医生14','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL),(25,1,'001000015','$2a$10$UbhCxVcBiGv1mKIx.oC/IOciIB1pM96SvAYIMjgW6K7VBuXuTYvXO','13800138019',NULL,'123456789@qq.com','DOCTOR',NULL,'医生15','ACTIVE',0,NULL,NULL,'2026-01-01 13:46:12',NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `doctor_full_info`
--

/*!50001 DROP VIEW IF EXISTS `doctor_full_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `doctor_full_info` AS select `d`.`id` AS `doctor_id`,`d`.`hospital_id` AS `hospital_id`,`d`.`employee_id` AS `employee_id`,`d`.`department_id` AS `department_id`,`d`.`title` AS `title`,`d`.`specialty` AS `specialty`,`d`.`introduction` AS `introduction`,`d`.`phone_bound` AS `phone_bound`,`d`.`created_time` AS `created_time`,`d`.`updated_time` AS `updated_time`,`u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`phone` AS `phone`,`u`.`email` AS `email`,`u`.`status` AS `status` from (`doctor` `d` left join `user` `u` on((`d`.`user_id` = `u`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `patient_full_info`
--

/*!50001 DROP VIEW IF EXISTS `patient_full_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `patient_full_info` AS select `p`.`id` AS `patient_id`,`p`.`id_card` AS `id_card`,`p`.`address` AS `address`,`p`.`emergency_contact` AS `emergency_contact`,`p`.`emergency_phone` AS `emergency_phone`,`p`.`medical_history` AS `medical_history`,`p`.`allergy_history` AS `allergy_history`,`p`.`created_time` AS `created_time`,`p`.`updated_time` AS `updated_time`,`u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`phone` AS `phone`,`u`.`email` AS `email`,`u`.`status` AS `status` from (`patient` `p` left join `user` `u` on((`p`.`user_id` = `u`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-02  7:08:22
