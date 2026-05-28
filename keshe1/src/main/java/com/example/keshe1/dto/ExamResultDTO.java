package com.example.keshe1.dto;

import lombok.Data;

@Data
public class ExamResultDTO {
    /**
     * 检查单ID
     */
    private Long id;

    /**
     * 填写的检查结果
     */
    private String result;
}