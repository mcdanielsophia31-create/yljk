package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Appointment;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {

    /**
     * 根据患者ID获取预约列表
     */
    List<Appointment> getByPatientId(Long patientId);

    /**
     * 根据医生ID获取预约列表
     */
    List<Appointment> getByDoctorId(Long doctorId);

    /**
     * 根据医生ID和状态获取预约列表
     */
    List<Appointment> getByDoctorIdAndStatus(Long doctorId, String status);

    /**
     * 获取医生的待确认和已确认预约（候诊队列）
     */
    List<Appointment> getWaitingAppointments(Long doctorId);

    /**
     * 根据患者ID和状态获取预约列表
     */
    List<Appointment> getByPatientIdAndStatus(Long patientId, String status);

    /**
     * 获取医生的预约列表（按状态筛选）
     */
    List<Appointment> getDoctorAppointments(Long doctorId, String status);

    int countByPatientId(Long id);

    List<Appointment> findByDoctorId(Long doctorId);

    boolean updateStatus(Long appointmentId, Long doctorId, String status, String rejectReason);
}