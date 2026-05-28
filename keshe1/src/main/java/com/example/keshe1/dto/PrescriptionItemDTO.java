package com.example.keshe1.dto;

import lombok.Data;

@Data
public class PrescriptionItemDTO {
    private Long medicineId;
    private String medicineName;
    private String genericName;
    private String specification;
    private String dosageUnit;
    private String frequency;
    private Integer quantity;
    private Integer totalQuantity;
    private Integer days;
    private String dosageForm;
    private Double price;
}