package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FineController {

    @Autowired
    private FineService fineService;

    /**
     * 查询学生罚款明细
     * GET /api/fines/{sno}
     */
    @GetMapping("/fines/{sno}")
    public List<Map<String, Object>> getFines(@PathVariable String sno) {
        System.out.println("=== 收到罚款查询请求，学号：" + sno + " ===");
        List<Map<String, Object>> result = fineService.getUnpaidFines(sno);
        System.out.println("=== 返回罚款记录数：" + result.size() + " ===");
        for (Map<String, Object> fine : result) {
            System.out.println("  - " + fine.get("bookName") +
                    " 超期" + fine.get("overdueDays") + "天 " +
                    "罚款¥" + fine.get("fineAmount"));
        }
        return result;
    }

    /**
     * 查询学生罚款总金额
     * GET /api/fines/{sno}/total
     */
    @GetMapping("/fines/{sno}/total")
    public Map<String, Object> getTotalFine(@PathVariable String sno) {
        Map<String, Object> result = new HashMap<>();
        result.put("sno", sno);
        result.put("totalAmount", fineService.getTotalUnpaidFineAmount(sno));
        result.put("unpaidCount", fineService.getUnpaidFineCount(sno));
        return result;
    }

    /**
     * 缴纳所有罚款
     * POST /api/fines/pay/{sno}
     */
    @PostMapping("/fines/pay/{sno}")
    public Map<String, Object> payAllFines(@PathVariable String sno) {
        System.out.println("=== 收到缴纳所有罚款请求，学号：" + sno + " ===");
        Map<String, Object> result = fineService.payAllFines(sno);
        System.out.println("=== 缴纳结果：" + result + " ===");
        return result;
    }

    /**
     * 缴纳单笔罚款
     * POST /api/fines/pay/{fineId}/single
     */
    @PostMapping("/fines/pay/{fineId}/single")
    public Map<String, Object> payFineById(@PathVariable Integer fineId) {
        System.out.println("=== 收到缴纳单笔罚款请求，ID：" + fineId + " ===");
        Map<String, Object> result = fineService.payFineById(fineId);
        return result;
    }

    /**
     * 手动生成罚款记录（管理员用）
     * POST /api/fines/generate/{sno}
     */
    @PostMapping("/fines/generate/{sno}")
    public Map<String, Object> generateFines(@PathVariable String sno) {
        System.out.println("=== 收到手动生成罚款请求，学号：" + sno + " ===");
        fineService.generateFinesForStudent(sno);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "罚款记录生成成功");
        return result;
    }

    /**
     * 兼容旧路径
     */
    @GetMapping("/fine/query/{sno}")
    public List<Map<String, Object>> queryFine(@PathVariable String sno) {
        return getFines(sno);
    }
}