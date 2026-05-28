package com.example.keshe1.controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.keshe1.entity.Medicine;
import com.example.keshe1.entity.MedicalRecord;
import com.example.keshe1.dto.PrescriptionItemDTO;
import com.example.keshe1.service.MedicineService;
import com.example.keshe1.service.MedicalRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/doctor")
public class PrescriptionController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 显示开药页面
     */
    @GetMapping("/prescription/{recordId}")
    public String showPrescriptionPage(@PathVariable Long recordId, Model model) {
        // 获取所有药品
        List<Medicine> medicines = medicineService.getAllMedicines();

        // 获取当前病历的治疗方案（如果已有）
        String currentTreatmentPlan = "";
        MedicalRecord medicalRecord = medicalRecordService.getById(recordId);
        if (medicalRecord != null && medicalRecord.getTreatmentPlan() != null) {
            currentTreatmentPlan = medicalRecord.getTreatmentPlan();
        }

        model.addAttribute("recordId", recordId);
        model.addAttribute("medicines", medicines);
        model.addAttribute("currentTreatmentPlan", currentTreatmentPlan);
        return "doctor/prescription";
    }

    /**
     * 获取病历对应的处方列表 (用于回显)
     */
    @GetMapping("/api/prescription-list/{recordId}")
    @ResponseBody
    public List<PrescriptionItemDTO> getPrescriptionList(@PathVariable Long recordId) {
        MedicalRecord record = medicalRecordService.getById(recordId);
        if (record == null || record.getTreatmentPlan() == null || record.getTreatmentPlan().isEmpty()) {
            return Collections.emptyList();
        }

        String plan = record.getTreatmentPlan().trim();

        if (!plan.startsWith("[")) {
            System.out.println("检测到旧版文本格式处方，跳过解析: " + plan);
            return Collections.emptyList();
        }

        try {
            // 尝试解析 JSON
            return objectMapper.readValue(plan,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PrescriptionItemDTO.class));
        } catch (Exception e) {
            // 如果解析失败，打印日志但不要抛出异常给前端，返回空列表即可
            System.err.println("处方 JSON 解析失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 搜索药品
     */
    @GetMapping("/api/medicines/search")
    @ResponseBody
    public List<Medicine> searchMedicines(@RequestParam(required = false) String keyword) {
        return medicineService.searchMedicines(keyword);
    }

    /**
     * 【核心修改】保存处方并自动更新库存
     * 使用用户自定义的 PrescriptionItemDTO
     */
    @PostMapping("/prescription/save")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public ResponseEntity<String> savePrescription(
            @RequestParam Long recordId,
            @RequestParam String treatmentPlanJson) {

        try {
            // 1. 获取旧的病历信息
            MedicalRecord record = medicalRecordService.getById(recordId);
            if (record == null) {
                return ResponseEntity.badRequest().body("病历不存在");
            }

            // 2. 解析【旧】处方 (数据库里原本存的)
            Map<Long, Integer> oldMap = new HashMap<>();
            String oldPlan = record.getTreatmentPlan();

            if (oldPlan != null && oldPlan.trim().startsWith("[")) {
                try {
                    List<PrescriptionItemDTO> oldList = objectMapper.readValue(oldPlan,
                            new TypeReference<List<PrescriptionItemDTO>>() {});

                    for (PrescriptionItemDTO item : oldList) {
                        if (item.getMedicineId() != null) {
                            // 【重点】优先使用 totalQuantity (总数量)，如果没有则用 quantity，再没有则默认为 1
                            Integer qty = item.getTotalQuantity();
                            if (qty == null) qty = item.getQuantity();
                            if (qty == null) qty = 1;

                            oldMap.put(item.getMedicineId(), oldMap.getOrDefault(item.getMedicineId(), 0) + qty);
                        }
                    }
                } catch (Exception e) {
                    // 旧数据解析失败忽略，不进行库存回滚
                    System.err.println("旧处方解析失败，跳过库存回滚: " + e.getMessage());
                }
            }

            // 3. 解析【新】处方 (前端传来的 JSON)
            Map<Long, Integer> newMap = new HashMap<>();
            List<PrescriptionItemDTO> newList = new ArrayList<>();

            // 关键修改：即使是空处方也要处理
            if (treatmentPlanJson != null && treatmentPlanJson.trim().startsWith("[")) {
                try {
                    newList = objectMapper.readValue(treatmentPlanJson,
                            new TypeReference<List<PrescriptionItemDTO>>() {});

                    for (PrescriptionItemDTO item : newList) {
                        if (item.getMedicineId() != null) {
                            // 【重点】同上，使用 totalQuantity
                            Integer qty = item.getTotalQuantity();
                            if (qty == null) qty = item.getQuantity();
                            if (qty == null) qty = 1;

                            newMap.put(item.getMedicineId(), newMap.getOrDefault(item.getMedicineId(), 0) + qty);
                        }
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("数据格式错误: " + e.getMessage());
                }
            } else {
                // 如果不是JSON格式，当作空处方处理
                System.out.println("收到非JSON格式处方，按空处方处理: " + treatmentPlanJson);
            }

            // 4. 【库存计算】对比差异 = 新数量 - 旧数量
            Set<Long> allMedicineIds = new HashSet<>();
            allMedicineIds.addAll(oldMap.keySet());
            allMedicineIds.addAll(newMap.keySet());

            for (Long medId : allMedicineIds) {
                int oldQty = oldMap.getOrDefault(medId, 0);
                int newQty = newMap.getOrDefault(medId, 0);
                int diff = newQty - oldQty;

                if (diff != 0) {
                    // 正数表示消耗库存，需要检查余量
                    if (diff > 0) {
                        Medicine med = medicineService.getById(medId);
                        if (med == null) {
                            throw new RuntimeException("药品ID " + medId + " 不存在");
                        }
                        if (med.getStockQuantity() < diff) {
                            throw new RuntimeException("药品【" + med.getName() + "】库存不足！剩余：" + med.getStockQuantity() + "， 需扣除：" + diff);
                        }
                    }

                    // 执行更新（负数会自动增加库存）
                    boolean updateSuccess = medicineService.updateStock(medId, diff);
                    if (!updateSuccess) {
                        throw new RuntimeException("库存更新失败，请重试");
                    }
                }
            }

            // 5. 更新病历表
            // 如果是空处方，保存为 "[]"
            String finalPlan = treatmentPlanJson;
            if (treatmentPlanJson == null || treatmentPlanJson.trim().isEmpty()) {
                finalPlan = "[]";
            }
            record.setTreatmentPlan(finalPlan);

            // 可以在这里计算总价并保存 record.setTotalPrice(...)
            // 如果是空处方，总价设为0
            medicalRecordService.updateById(record);

            return ResponseEntity.ok("success");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("保存异常: " + e.getMessage());
        }
    }
}