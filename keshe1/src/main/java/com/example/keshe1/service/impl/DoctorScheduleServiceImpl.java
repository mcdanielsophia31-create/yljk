package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.DoctorSchedule;
import com.example.keshe1.mapper.DoctorScheduleMapper;
import com.example.keshe1.service.DoctorScheduleService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements DoctorScheduleService {
    
    @Override
    public List<DoctorSchedule> getSchedulesByDateRangeAndDepartment(String startDate, String endDate, Long departmentId, Long hospitalId) {
        // 实现根据日期范围和科室获取排班信息
        return this.list(new LambdaQueryWrapper<DoctorSchedule>()
            .ge(DoctorSchedule::getScheduleDate, startDate)
            .le(DoctorSchedule::getScheduleDate, endDate)
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId)
            .eq(DoctorSchedule::getScheduleStatus, "VALID"));
    }
    
    @Override
    public void updateSchedulesWithBusinessLogic(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId, List<Map<String, Object>> doctors) {
        // 这个方法的具体业务逻辑已在ScheduleController中实现
        // DoctorScheduleService仅处理DoctorSchedule表的基本操作
        // 完整的跨表业务逻辑由Controller协调多个Service完成
    }
    
    @Override
    public void updateSchedulesForDateAndTimeSlot(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId, List<Map<String, Object>> doctors) {
        // 删除该日期和时段的现有排班
        LambdaQueryWrapper<DoctorSchedule> deleteWrapper = new LambdaQueryWrapper<DoctorSchedule>()
            .eq(DoctorSchedule::getScheduleDate, scheduleDate)
            .eq(DoctorSchedule::getTimeSlot, timeSlot)
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId);
        this.remove(deleteWrapper);
        
        // 为选中的医生添加新的排班
        for (Map<String, Object> doctorData : doctors) {
            String doctorName = (String) doctorData.get("doctorName");
            String employeeId = (String) doctorData.get("employeeId");
            Integer registrationQuota = Integer.valueOf(doctorData.get("registrationQuota").toString());
            
            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setScheduleDate(LocalDate.parse(scheduleDate));
            schedule.setTimeSlot(timeSlot);
            schedule.setDoctorName(doctorName);
            schedule.setEmployeeId(employeeId);
            schedule.setRegistrationQuota(registrationQuota);
            schedule.setDepartmentId(departmentId);
            schedule.setHospitalId(hospitalId);
            schedule.setScheduleStatus("VALID");
            schedule.setRegisteredCount(0);
            
            // 设置默认挂号费用
            schedule.setRegistrationFee(new BigDecimal("20.0"));
            
            this.save(schedule);
        }
    }
    
    @Override
    public void resetWeekSchedules(String startDate, String endDate, Long departmentId, Long hospitalId) {
        // 删除指定周的排班
        LambdaQueryWrapper<DoctorSchedule> deleteWrapper = new LambdaQueryWrapper<DoctorSchedule>()
            .ge(DoctorSchedule::getScheduleDate, startDate)
            .le(DoctorSchedule::getScheduleDate, endDate)
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId);
        this.remove(deleteWrapper);
    }
    
    @Override
    public void copyPreviousWeekSchedules(String startDate, String endDate, Long departmentId, Long hospitalId) {
        // 计算上周的日期范围
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate prevStart = start.minusWeeks(1);
        LocalDate prevEnd = end.minusWeeks(1);
        
        // 获取上周的排班数据
        List<DoctorSchedule> prevSchedules = this.list(new LambdaQueryWrapper<DoctorSchedule>()
            .ge(DoctorSchedule::getScheduleDate, prevStart.toString())
            .le(DoctorSchedule::getScheduleDate, prevEnd.toString())
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId)
            .ne(DoctorSchedule::getScheduleStatus, "CANCELLED"));
        
        // 删除当前周的排班
        LambdaQueryWrapper<DoctorSchedule> deleteWrapper = new LambdaQueryWrapper<DoctorSchedule>()
            .ge(DoctorSchedule::getScheduleDate, startDate)
            .le(DoctorSchedule::getScheduleDate, endDate)
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId);
        this.remove(deleteWrapper);
        
        // 复制上周的排班到当前周
        for (DoctorSchedule prevSchedule : prevSchedules) {
            DoctorSchedule newSchedule = new DoctorSchedule();
            
            // 复制属性并调整日期
            newSchedule.setDoctorName(prevSchedule.getDoctorName());
            newSchedule.setEmployeeId(prevSchedule.getEmployeeId());
            newSchedule.setScheduleDate(LocalDate.parse(startDate).plusDays(
                LocalDate.parse(prevSchedule.getScheduleDate().toString()).getDayOfWeek().getValue() - 1));
            newSchedule.setTimeSlot(prevSchedule.getTimeSlot());
            newSchedule.setRegistrationQuota(prevSchedule.getRegistrationQuota());
            newSchedule.setDepartmentId(prevSchedule.getDepartmentId());
            newSchedule.setHospitalId(prevSchedule.getHospitalId());
            newSchedule.setScheduleStatus(prevSchedule.getScheduleStatus());
            // 复制挂号费用，保持BigDecimal类型
            newSchedule.setRegistrationFee(prevSchedule.getRegistrationFee());
            newSchedule.setRoomNumber(prevSchedule.getRoomNumber());
            newSchedule.setRegisteredCount(0); // 重置已挂号数量
            
            this.save(newSchedule);
        }
    }
    
    @Override
    public File exportScheduleToExcel(String exportType, String startDate, String endDate, Long departmentId, Long hospitalId) {
        try {
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("排班表");
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"医生工号", "医生真名", "排班日期", "时间段", "诊室号", "诊号数量"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 获取排班数据
            List<DoctorSchedule> schedules = new ArrayList<>();
            if ("week".equals(exportType)) {
                schedules = this.getSchedulesByDateRangeAndDepartment(startDate, endDate, departmentId, hospitalId);
            } else if ("month".equals(exportType)) {
                // 月排班数据需要计算月份范围
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                LocalDate monthStart = start.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate monthEnd = end.with(TemporalAdjusters.lastDayOfMonth());
                
                schedules = this.list(new LambdaQueryWrapper<DoctorSchedule>()
                    .ge(DoctorSchedule::getScheduleDate, monthStart.toString())
                    .le(DoctorSchedule::getScheduleDate, monthEnd.toString())
                    .eq(DoctorSchedule::getDepartmentId, departmentId)
                    .eq(DoctorSchedule::getHospitalId, hospitalId)
                    .eq(DoctorSchedule::getScheduleStatus, "VALID"));
            }
            
            // 按日期和时间段排序
            schedules.sort((s1, s2) -> {
                int dateCompare = s1.getScheduleDate().compareTo(s2.getScheduleDate());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return s1.getTimeSlot().compareTo(s2.getTimeSlot());
            });
            
            // 填充数据
            int rowNum = 1;
            for (DoctorSchedule schedule : schedules) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(schedule.getEmployeeId());
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(schedule.getDoctorName());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(schedule.getScheduleDate().toString());
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(schedule.getTimeSlot());
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(schedule.getRoomNumber() != null ? schedule.getRoomNumber() : "待分配");
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(schedule.getRegistrationQuota());
                cell5.setCellStyle(dataStyle);
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 生成临时文件
            String fileName = exportType.equals("week") ? "周排班表.xlsx" : "月排班表.xlsx";
            File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
            
            try (FileOutputStream fileOut = new FileOutputStream(tempFile)) {
                workbook.write(fileOut);
            }
            
            workbook.close();
            
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Map<String, Integer> getScheduleStats(String scheduleDate, String timeSlot, Long departmentId, Long hospitalId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 这里需要实现统计逻辑
        // 暂时返回示例数据
        stats.put("occupiedDoctors", 0);
        stats.put("availableRooms", 10);
        
        return stats;
    }
    
    @Override
    public List<DoctorSchedule> getDoctorSchedules(String employeeId, Long departmentId, Long hospitalId) {
        // 查询指定医生的排班信息
        return this.list(new LambdaQueryWrapper<DoctorSchedule>()
            .eq(DoctorSchedule::getEmployeeId, employeeId)
            .eq(DoctorSchedule::getDepartmentId, departmentId)
            .eq(DoctorSchedule::getHospitalId, hospitalId)
            .eq(DoctorSchedule::getScheduleStatus, "VALID")
            .orderByAsc(DoctorSchedule::getScheduleDate)
            .orderByAsc(DoctorSchedule::getTimeSlot));
    }
}