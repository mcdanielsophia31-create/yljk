package com.example.keshe1.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicalRecordPriceDTO {
    private Long recordId;
    private BigDecimal registrationFee;  // 挂号费
    private BigDecimal medicineFee;      // 药费
    private BigDecimal examinationFee;   // 检查费
    private BigDecimal totalPrice;       // 总金额
}