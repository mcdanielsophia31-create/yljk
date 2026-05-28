package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Appointment;
import com.example.keshe1.mapper.AppointmentMapper;
import com.example.keshe1.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public List<Appointment> getByPatientId(Long patientId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.orderByDesc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public List<Appointment> getByDoctorId(Long doctorId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        queryWrapper.orderByDesc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public List<Appointment> getByDoctorIdAndStatus(Long doctorId, String status) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public List<Appointment> getWaitingAppointments(Long doctorId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        queryWrapper.in("status", "待确认", "已确认");
        queryWrapper.orderByAsc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public List<Appointment> getByPatientIdAndStatus(Long patientId, String status) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public List<Appointment> getDoctorAppointments(Long doctorId, String status) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("appointment_time");
        return list(queryWrapper);
    }

    @Override
    public int countByPatientId(Long patientId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        // 如果想排除已取消的预约，可以加上下面的条件
         queryWrapper.ne("status", "已取消");
        return Math.toIntExact(baseMapper.selectCount(queryWrapper));
    }
    /**
     * 根据医生ID查询预约记录
     */
    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("doctor_id", doctorId);
        queryWrapper.orderByDesc("appointment_date", "time_slot");
        return appointmentMapper.selectList(queryWrapper);
    }

    /**
     * 更新预约状态
     */
    @Override
    public boolean updateStatus(Long appointmentId, Long doctorId, String status, String rejectReason) {
        // 验证预约是否属于该医生
        Appointment appointment = getById(appointmentId);
        if (appointment == null || !appointment.getDoctorId().equals(doctorId)) {
            return false;
        }

        // 更新状态
        appointment.setStatus(status);
        if ("已拒绝".equals(status) && rejectReason != null) {
            appointment.setRejectReason(rejectReason);
        }
        return updateById(appointment);
    }
}