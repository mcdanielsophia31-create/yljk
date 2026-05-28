package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据电话号码查找用户
     */
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(@Param("phone") String phone);
    
    /**
     * 根据工号查找用户
     */
    @Select("SELECT * FROM user WHERE employee_id = #{employeeId}")
    User findByEmployeeId(@Param("employeeId") String employeeId);
    
    /**
     * 根据科室ID和医院ID获取医生用户列表
     */
    @Select("SELECT u.* FROM user u " +
            "JOIN doctor d ON u.employee_id = d.employee_id " +
            "WHERE d.department_id = #{departmentId} AND u.hospital_id = #{hospitalId} AND u.user_type = 'DOCTOR'")
    List<User> getDoctorsByDepartmentAndHospital(@Param("departmentId") Long departmentId,
                                                 @Param("hospitalId") Long hospitalId);
}