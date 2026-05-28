package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.dto.DoctorDTO;
import com.example.keshe1.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    // 新增方法 - 移除不存在的字段
    @Select("SELECT d.id, u.real_name as name, d.employee_id as employeeId, d.gender, " +
            "u.phone, u.email, d.title, d.specialty, d.phone_bound as phoneBound, d.hospital_id as hospitalId, " +
            "d.department_id as departmentId " +
            "FROM doctor d " +
            "INNER JOIN user u ON d.user_id = u.id " +
            "WHERE d.hospital_id = #{hospitalId}")
    List<DoctorDTO> selectDoctorsWithUserInfoByHospitalId(@Param("hospitalId") Long hospitalId);

    @Select("SELECT d.id, u.real_name as name, d.employee_id as employeeId, d.gender, " +
            "u.phone, u.email, d.title, d.specialty, d.phone_bound as phoneBound, d.hospital_id as hospitalId, " +
            "d.department_id as departmentId " +
            "FROM doctor d " +
            "INNER JOIN user u ON d.user_id = u.id " +
            "WHERE d.hospital_id = #{hospitalId} " +
            "AND u.real_name LIKE CONCAT('%', #{name}, '%')")
    List<DoctorDTO> selectDoctorsWithUserInfoByHospitalIdAndName(@Param("hospitalId") Long hospitalId,
                                                                 @Param("name") String name);

    @Select("SELECT d.id, u.real_name as name, d.employee_id as employeeId, d.gender, " +
            "u.phone, u.email, d.title, d.specialty, d.phone_bound as phoneBound, d.hospital_id as hospitalId, " +
            "d.department_id as departmentId " +
            "FROM doctor d " +
            "INNER JOIN user u ON d.user_id = u.id " +
            "WHERE u.real_name LIKE CONCAT('%', #{name}, '%')")
    List<DoctorDTO> selectDoctorsWithUserInfoByName(@Param("name") String name);

    @Select("SELECT d.id, u.real_name as name, d.employee_id as employeeId, d.gender, " +
            "u.phone, u.email, d.title, d.specialty, d.phone_bound as phoneBound, d.hospital_id as hospitalId, " +
            "d.department_id as departmentId " +
            "FROM doctor d " +
            "INNER JOIN user u ON d.user_id = u.id")
    List<DoctorDTO> selectAllDoctorsWithUserInfo();
}