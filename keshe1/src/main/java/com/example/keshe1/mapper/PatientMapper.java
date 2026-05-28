package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    //hsy加
    /**
     * 根据医院ID查询所有患者ID
     */
    @Select("SELECT p.id FROM patient p " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId}")
    List<Long> selectIdsByHospitalId(@Param("hospitalId") Long hospitalId);

    /**
     * 根据医院ID和患者姓名搜索患者ID（通过user表的real_name）
     */
    @Select("SELECT p.id FROM patient p " +
            "INNER JOIN user u ON p.user_id = u.id " +
            "WHERE u.hospital_id = #{hospitalId} " +
            "AND u.real_name LIKE CONCAT('%', #{name}, '%')")
    List<Long> selectIdsByHospitalIdAndName(@Param("hospitalId") Long hospitalId,
                                            @Param("name") String name);
}