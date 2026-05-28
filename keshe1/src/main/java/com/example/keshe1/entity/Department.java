package com.example.keshe1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {
    
    /**
     * 科室ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属医院ID
     */
    private Long hospitalId;

    /**
     * 科室名称
     */
    private String name;

    /**
     * 科室描述
     */
    private String description;
    
    /**
     * 诊室数
     */
    private Integer roomCount;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(Integer roomCount) {
        this.roomCount = roomCount;
    }
}