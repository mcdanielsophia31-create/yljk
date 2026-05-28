package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doctor")
public class Doctor extends BaseEntity {

    /**
     * 医生ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 所属医院ID
     */
    private Long hospitalId;

    /**
     * 工号（9位数字，前3位为医院编码，后6位为医生序号）
     */
    private String employeeId;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 所属科室ID
     */
    private Long departmentId;

    /**
     * 科室名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 职称
     */
    private String title;

    /**
     * 专长
     */
    private String specialty;

    /**
     * 医生简介
     */
    private String introduction;

    /**
     * 电话号码是否已绑定
     */
    private Boolean phoneBound = false;

    /**
     * 挂号费（从数据库触发器自动设置）
     */
    private BigDecimal registrationFee = BigDecimal.ZERO;

    /**
     * 获取职称对应的挂号费
     */
    public BigDecimal getRegistrationFeeByTitle() {
        if (this.registrationFee != null) {
            return this.registrationFee;
        }

        // 如果数据库中没有设置，根据职称设置默认值
        if (title != null) {
            switch (title) {
                case "主任医师":
                    return new BigDecimal("50.00");
                case "副主任医师":
                    return new BigDecimal("30.00");
                case "主治医师":
                    return new BigDecimal("20.00");
                case "住院医师":
                    return new BigDecimal("10.00");
                default:
                    return new BigDecimal("10.00");
            }
        }
        return new BigDecimal("10.00");
    }
}