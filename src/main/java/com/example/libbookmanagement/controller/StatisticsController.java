package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // 直接返回数据，不做任何包裹
    @GetMapping("/personal/{sno}")
    public Map<String, Object> getPersonalInfo(@PathVariable String sno) {
        System.out.println("=== 收到个人查询请求，学号：" + sno + " ===");
        Map<String, Object> result = statisticsService.getPersonalBorrowInfo(sno);
        System.out.println("=== 返回个人查询数据：" + result + " ===");
        return result;
    }

    // 直接返回列表数据
    @GetMapping("/students/ranking")
    public List<Map<String, Object>> getStudentRank() {
        return statisticsService.getMonthlyStudentRanking();
    }

    // 直接返回列表数据
    @GetMapping("/books/ranking")
    public List<Map<String, Object>> getBookRank() {
        return statisticsService.getMonthlyBookRanking();
    }
}