package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // 建议使用 @Data 简化 getter/setter
@EqualsAndHashCode(callSuper = true)
@TableName("examination")
public class Examination extends BaseEntity {

    // ... 原有的 id, patientId, doctorId, recordId, examinationDate 等字段保持不变 ...

    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long recordId;
    private LocalDate examinationDate;
    private Long examinationItemId;
    private String status;
    private String examinationResult;


    /**
     * 患者姓名 (来自存储过程: patientName)
     */
    @TableField(exist = false)
    private String patientName;

    /**
     * 医生姓名 (来自存储过程: doctorName)
     */
    @TableField(exist = false)
    private String doctorName;

    /**
     * 检查项目名称 (来自存储过程: examinationItemName)
     */
    @TableField(exist = false)
    private String examinationItemName;

    /**
     * 检查类型 (来自存储过程: examinationType)
     */
    @TableField(exist = false)
    private String examinationType;

    /**
     * 价格 (来自存储过程: price)
     */
    @TableField(exist = false)
    private BigDecimal price;

    /**
     * 病历日期 (来自存储过程: recordDate)
     */
    @TableField(exist = false)
    private LocalDate recordDate;
}