package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medicine")
public class Medicine {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("generic_name")
    private String genericName;

    @TableField("dosage_form")
    private String dosageForm;

    private String specification;

    @TableField("dosage_unit")
    private String dosageUnit;

    private String manufacturer;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("stock_unit")
    private String stockUnit;

    private BigDecimal price;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;


}
