
package com.example.keshe1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.keshe1.entity.Medicine;
import com.example.keshe1.mapper.MedicineMapper;
import com.example.keshe1.service.MedicineService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    @Override
    public List<Medicine> getAllMedicines() {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Medicine.class, field ->
                !field.getColumn().equals("created_time") &&
                        !field.getColumn().equals("updated_time")
        );
        wrapper.orderByAsc(Medicine::getName);
        return this.list(wrapper);
    }

    @Override
    public List<Medicine> searchMedicines(String keyword) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Medicine::getName, keyword)
                    .or()
                    .like(Medicine::getGenericName, keyword)
                    .or()
                    .like(Medicine::getManufacturer, keyword);
        }
        wrapper.select(Medicine.class, field ->
                !field.getColumn().equals("created_time") &&
                        !field.getColumn().equals("updated_time")
        );
        wrapper.orderByAsc(Medicine::getName);
        return this.list(wrapper);
    }
    @Override
    public boolean updateStock(Long medicineId, Integer changeQuantity) {
        if (changeQuantity == 0) return true;

        // 使用 MyBatis Plus 的 UpdateWrapper 执行原子更新
        // SQL: UPDATE medicine SET stock_quantity = stock_quantity - changeQuantity WHERE id = medicineId
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Medicine>()
                .setSql("stock_quantity = stock_quantity - " + changeQuantity)
                .eq("id", medicineId));
    }
}