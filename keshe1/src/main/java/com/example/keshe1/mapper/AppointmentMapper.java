package com.example.keshe1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.keshe1.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 医生端上半部分
     */
    // 统计某天的预约总数 (用于计算今日和昨日数据)
    @Select("SELECT COUNT(*) FROM appointment WHERE doctor_id = #{doctorId} AND appointment_date = #{date}")
    Integer countByDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // 统计待确认状态的预约数
    @Select("SELECT COUNT(*) FROM appointment WHERE doctor_id = #{doctorId} AND status = '待确认'")
    Integer countPending(@Param("doctorId") Long doctorId);
}