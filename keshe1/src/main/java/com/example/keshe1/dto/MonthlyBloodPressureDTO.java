package com.example.keshe1.dto;

public class MonthlyBloodPressureDTO {
    private String yearMonth;          // 月份
    private Double avgSystolic;     // 平均收缩压
    private Double avgDiastolic;    // 平均舒张压
    private Integer validPatientCount; // 有效患者数

    public MonthlyBloodPressureDTO() {
    }

    public MonthlyBloodPressureDTO(Integer month, Double avgSystolic, Double avgDiastolic, Integer validPatientCount) {
        this.yearMonth = yearMonth;
        this.avgSystolic = avgSystolic;
        this.avgDiastolic = avgDiastolic;
        this.validPatientCount = validPatientCount;
    }

    // Getter和Setter方法
    public String getYearMonth() {return yearMonth;}

    public void setYearMonth(String yearMonth) {this.yearMonth = yearMonth;}

    public Double getAvgSystolic() {
        return avgSystolic;
    }

    public void setAvgSystolic(Double avgSystolic) {
        this.avgSystolic = avgSystolic;
    }

    public Double getAvgDiastolic() {
        return avgDiastolic;
    }

    public void setAvgDiastolic(Double avgDiastolic) {
        this.avgDiastolic = avgDiastolic;
    }

    public Integer getValidPatientCount() {
        return validPatientCount;
    }

    public void setValidPatientCount(Integer validPatientCount) {
        this.validPatientCount = validPatientCount;
    }
}