package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检查项目实体类
 */
@Data
@TableName("examination_item")
public class ExaminationItem {

    /**
     * 检查项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 检查类型 (例如：临床检验、医学影像-CT)
     */
    private String examinationType;

    /**
     * 检查项目名称 (例如：血常规、头颅CT平扫)
     */
    private String examinationItem;

    /**
     * 价格
     */
    private BigDecimal price;
}