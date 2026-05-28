# 医院管理系统数据库结构优化说明

## 优化目标
解决user、doctor、patient表之间的数据冗余问题，确保数据一致性。

## 主要改动

### 1. 数据库表结构优化
- 移除doctor表中的冗余字段：name, phone, email
- 移除patient表中的冗余字段：name, phone, email
- 添加外键约束确保数据完整性
- 创建视图以方便查询用户完整信息

### 2. 实体类更新
- 更新Doctor.java实体类，移除name, phone, email字段
- 更新Patient.java实体类，移除name, phone, email字段
- 保留userId关联字段以连接用户信息

### 3. 服务层增强
- 创建UserInfoService接口及其实现类
- 提供关联查询功能，确保用户信息一致性
- 提供统一的用户信息更新方法

### 4. 数据库脚本
- 创建optimize_database_structure.sql优化脚本
- 创建apply_database_optimization.bat执行脚本

## 数据一致性保证
- 用户基本信息统一在user表中维护
- 通过userId关联字段获取用户完整信息
- 更新用户信息时通过服务层确保一致性

## 使用说明
1. 在部署前先执行数据库优化脚本：
   ```
   apply_database_optimization.bat
   ```
2. 重新启动应用以应用新的实体类结构
3. 现有数据会自动迁移，确保业务连续性

## 影响评估
- 正面影响：消除数据冗余，提高数据一致性，减少存储空间
- 兼容性：保持向后兼容，现有功能不受影响
- 性能：通过关联查询可能略微影响查询性能，但可以通过索引优化