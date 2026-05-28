# 医疗健康管理系统

## 系统概述

这是一个基于Spring Boot + Thymeleaf的医疗健康管理系统，支持病人、医生和医院机构（管理员）三类用户角色的独立登录和功能访问。

## 技术架构

- 后端：Spring Boot 2.7.6
- 前端：Thymeleaf模板引擎 + Bootstrap 5
- 数据库：MySQL 8.0
- ORM框架：MyBatis-Plus 3.5.3.1

## 功能特性

### 用户角色
1. **病人**
   - 自助注册账号
   - 查看个人信息
   - 预约挂号
   - 查看病历记录

2. **医生**
   - 由管理员创建账号
   - 查看病人列表
   - 管理预约信息
   - 记录病历信息

3. **医院机构（管理员）**
   - 登录系统
   - 创建医生账号
   - 管理所有病人和医生信息

### 安全特性
- MD5密码加密存储
- 电话号码唯一性验证
- 用户状态管理（激活/未激活/锁定）
- Session会话管理

## 系统入口

- 主入口：http://localhost:8080
- 病人登录：http://localhost:8080/patient/login
- 医生登录：http://localhost:8080/doctor/login
- 管理员登录：http://localhost:8080/admin/login
- 病人注册：http://localhost:8080/patient/register

## 默认账号

### 病人账号
- 用户名：patient1
- 密码：123456

### 医生账号
- 用户名：doctor1
- 密码：123456

### 管理员账号
- 用户名：admin
- 密码：123456

## 部署说明

1. 确保已安装Java 8+和MySQL 8.0+
2. 创建MySQL数据库：
   ```sql
   CREATE DATABASE hospital_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 执行数据库脚本：`src/main/resources/db/hospital_management_system.sql`
4. 修改`src/main/resources/application.properties`中的数据库连接配置
5. 编译打包：
   ```bash
   mvn clean package
   ```
6. 运行应用：
   ```bash
   java -jar target/keshe1-0.0.1-SNAPSHOT.jar
   ```
7. 访问系统：http://localhost:8080

## 目录结构

```
src/
├── main/
│   ├── java/
│   │   └── com/example/keshe1/
│   │       ├── controller/     # 控制器层
│   │       ├── entity/         # 实体类
│   │       ├── mapper/         # 数据访问层
│   │       ├── service/        # 业务逻辑层
│   │       └── Keshe1Application.java  # 启动类
│   └── resources/
│       ├── templates/          # Thymeleaf模板文件
│       │   ├── admin/          # 管理员页面
│       │   ├── doctor/         # 医生页面
│       │   └── patient/        # 病人页面
│       ├── static/             # 静态资源文件
│       ├── db/                 # 数据库脚本
│       └── application.properties  # 配置文件
```

## 开发说明

1. 控制器层负责处理HTTP请求和页面跳转
2. 服务层实现业务逻辑
3. Mapper层负责数据库操作
4. 前端页面使用Thymeleaf模板引擎渲染
5. 所有用户密码均使用MD5加密存储

## 注意事项

1. 医生账号只能由管理员创建，病人可以自助注册
2. 新注册的用户默认状态为"未激活"，需要管理员审核激活(wen:ai说的，不是我说的)
3. 系统使用Session进行用户身份验证和状态保持
4. 电话号码在系统中具有唯一性约束