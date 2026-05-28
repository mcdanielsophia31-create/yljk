package com.example.keshe1.dto;

import lombok.Data;
import java.util.List;

@Data
public class PrescribeDTO {
    /**
     * 病历ID
     */
    private Long recordId;

    /**
     * 选中的检查项目ID列表
     */
    private List<Long> itemIds;
}