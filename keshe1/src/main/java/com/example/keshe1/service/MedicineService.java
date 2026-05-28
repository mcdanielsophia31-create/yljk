package com.example.keshe1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.keshe1.entity.Medicine;
import java.util.List;

public interface MedicineService extends IService<Medicine> {

    List<Medicine> getAllMedicines();

    List<Medicine> searchMedicines(String keyword);

    /**
     * 更新库存（支持增加或减少）
     * @param medicineId 药品ID
     * @param changeQuantity 变化数量（正数代表消耗库存，负数代表退回库存，但在SQL逻辑中我们通常用 stock - change）
     * @return 是否成功
     */
    boolean updateStock(Long medicineId, Integer changeQuantity);
}