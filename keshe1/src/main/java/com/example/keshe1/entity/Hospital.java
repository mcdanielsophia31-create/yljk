package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

/**
 * 医院实体类
 */
@EqualsAndHashCode(callSuper = true)
@TableName("hospital")
public class Hospital extends BaseEntity {
    
    /**
     * 医院ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 医院名称
     */
    private String name;

    /**
     * 医院地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 医院简介
     */
    private String description;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}