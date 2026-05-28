
# ##############################
#  wen:可以参考这个进行对每个功能的优化，不一定要完全做完
# ##############################
# 医疗健康管理系统完整版说明文档

## 系统概述

这是一个基于Spring Boot + Thymeleaf的医疗健康管理系统，支持病人、医生和医院机构（管理员）三类用户角色的完整功能实现。

## 技术架构

- 后端：Spring Boot 2.7.6
- 前端：Thymeleaf模板引擎 + Bootstrap 5
- 数据库：MySQL 8.0
- ORM框架：MyBatis-Plus 3.5.3.1
- 安全：MD5密码加密

## 功能模块

### 病人端功能（Patient）

#### 基础功能
- 病人注册
- 病人登录
- 个人信息管理

#### 医疗记录管理
- 电子病历查看
- 诊断记录查询
- 检查报告查询
- 用药历史查询

#### 预约服务
- 预约挂号
- 医生排班查询
- 预约确认
- 预约状态管理
- 候诊提醒

#### 健康管理
- 健康档案管理
- 体检报告上传
- 健康指标记录
- 血压记录
- 血糖记录
- 慢病自我管理
- 健康计划查看
- 运动指导查看
- 饮食指导查看

#### 疫苗服务
- 疫苗预约
- 疫苗接种提醒
- 疫苗接种记录查询

### 医生端功能（Doctor）

#### 基础功能
- 医生登录
- 医生信息管理

#### 患者管理
- 患者信息管理
- 患者列表管理

#### 病历管理
- 电子病历管理
- 患者基本信息维护
- 诊断记录填写
- 检查单开具
- 检查报告查看
- 用药方案制定
- 电子处方开具

#### 预约管理
- 预约管理
- 医生排班管理
- 号源管理
- 预约确认
- 候诊队列管理

#### 在线服务
- 在线问诊处理
- 文字问诊回复
- 视频问诊服务
- 远程问诊管理

#### 专业分析
- 检查结果分析
- 病历历史分析
- 慢病随访管理
- 健康计划制定
- 个性化健康建议
- 异常指标预警
- 疫苗接种建议
- 禁忌症提示

### 管理员功能（Admin）

#### 用户管理
- 病人管理
- 医生管理
- 创建医生账号

#### 系统管理
- 科室管理
- 系统统计
- 权限管理

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

## 数据库设计

### 核心表结构

1. **用户表（user）**
   - 存储系统所有用户的登录信息
   - 包含用户名、密码（MD5加密）、电话、邮箱等字段
   - 通过user_type字段区分用户角色

2. **病人表（patient）**
   - 存储病人的详细个人信息
   - 通过user_id关联到用户表

3. **医生表（doctor）**
   - 存储医生的详细个人信息
   - 通过department_id关联到科室表

4. **科室表（department）**
   - 存储医院科室信息

5. **预约表（appointment）**
   - 存储病人预约信息
   - 关联病人和医生

6. **病历表（medical_record）**
   - 存储电子病历信息
   - 关联病人和医生

7. **诊断表（diagnosis）**
   - 存储诊断记录
   - 关联到病历表

8. **检查表（examination）**
   - 存储检查报告
   - 关联到病历表

9. **药品表（medicine）**
   - 存储药品信息

10. **用药表（medication）**
    - 存储用药记录
    - 关联到病历表和药品表

11. **健康档案表（health_profile）**
    - 存储病人健康档案信息

12. **体检报告表（physical_exam）**
    - 存储体检报告信息

13. **健康指标表（health_indicator）**
    - 存储健康指标数据

## 安全特性

1. **密码加密**
   - 使用MD5算法对用户密码进行加密存储
   - 登录时对输入密码进行相同加密后比对

2. **权限控制**
   - 通过用户类型（PATIENT/DOCTOR/ADMIN）控制访问权限
   - 不同角色只能访问对应的功能模块

3. **会话管理**
   - 使用HttpSession进行用户状态管理
   - 登录超时自动跳转到登录页面

4. **数据验证**
   - 电话号码唯一性约束
   - 必填字段验证
   - 数据格式验证

## 部署说明

### 环境要求
- Java 8 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.6 或更高版本

### 部署步骤

1. **数据库准备**
   ```sql
   CREATE DATABASE hospital_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   执行数据库脚本：`src/main/resources/db/hospital_management_system.sql`

2. **配置文件修改**
   编辑`src/main/resources/application.properties`文件，修改数据库连接配置：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hospital_management?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **编译打包**
   ```bash
   mvn clean package
   ```

4. **运行应用**
   ```bash
   java -jar target/keshe1-0.0.1-SNAPSHOT.jar
   ```

5. **访问系统**
   打开浏览器访问：http://localhost:8080

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

### 控制器层
- 使用@Controller注解，返回Thymeleaf模板视图
- 通过@RequestMapping注解定义URL映射
- 使用@RequestParam和@ModelAttribute处理请求参数
- 通过Model对象向页面传递数据

### 服务层
- 使用@Service注解标记服务类
- 通过@Autowired注入Mapper和其他服务
- 实现具体的业务逻辑处理

### 数据访问层
- 使用MyBatis-Plus提供的BaseMapper接口
- 通过@Mapper注解标记Mapper接口
- 自定义复杂查询方法

### 前端页面
- 使用Thymeleaf模板引擎渲染HTML
- 通过th:*属性绑定后端数据
- 使用Bootstrap 5实现响应式布局

### 安全机制
- 所有用户密码均使用MD5加密存储
- 通过Session进行用户身份验证
- 每个请求都会验证用户权限

## 扩展建议

1. **安全性增强**
   - 升级到更安全的密码加密算法（如BCrypt）
   - 添加CSRF防护
   - 实现更细粒度的权限控制

2. **性能优化**
   - 添加Redis缓存常用数据
   - 实现数据库读写分离
   - 添加数据库索引优化查询性能

3. **功能扩展**
   - 添加消息通知系统
   - 实现移动端适配
   - 添加数据分析报表功能

4. **监控运维**
   - 集成Spring Boot Actuator
   - 添加日志收集和分析
   - 实现健康检查和自动重启

## 常见问题

1. **无法连接数据库**
   - 检查application.properties中的数据库配置
   - 确认MySQL服务是否启动
   - 检查数据库用户名和密码是否正确

2. **页面无法访问**
   - 检查控制器URL映射是否正确
   - 确认Thymeleaf模板文件是否存在
   - 检查是否有权限拦截

3. **密码验证失败**
   - 确认密码加密算法是否一致
   - 检查数据库中存储的密码是否正确加密
   - 验证登录逻辑是否正确

4. **数据无法保存**
   - 检查实体类字段与数据库表结构是否匹配
   - 确认MyBatis-Plus配置是否正确
   - 验证数据库连接是否正常

## 技术支持

如有任何技术问题，请参考以下资源：
- Spring Boot官方文档：https://spring.io/projects/spring-boot
- MyBatis-Plus官方文档：https://baomidou.com/
- Thymeleaf官方文档：https://www.thymeleaf.org/
- Bootstrap官方文档：https://getbootstrap.com/