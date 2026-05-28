package com.example.keshe1.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DoctorDashboardStatsDTO {
    // ===========================
    // 1. 顶部统计卡片 (原有字段)
    // ===========================

    // 1. 今日挂号量
    private Integer todayAppointments;
    // 挂号量趋势 (例如: "+10%", "-5%")
    private String appointmentTrend;
    // 趋势是否为增长 (用于前端显示颜色，true显示红色或绿色)
    private Boolean isTrendPositive;

    // 2. 待确诊患者 (待处理)
    private Integer pendingAppointments;

    // 3. 本月接诊人次
    private Integer monthlyVisits;

    // 4. 管理患者总数
    private Integer totalPatients;

    private Map<String, Integer> ageDistribution;
}