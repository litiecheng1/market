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

    @GetMapping("/personal/{sno}")
    public Map<String, Object> getPersonalInfo(@PathVariable String sno) {
        System.out.println("=== 收到个人查询请求，学号：" + sno + " ===");
        Map<String, Object> result = statisticsService.getPersonalBorrowInfo(sno);
        System.out.println("=== 返回个人查询数据：" + result + " ===");
        return result;
    }

    @GetMapping("/students/ranking")
    public List<Map<String, Object>> getStudentRank() {
        System.out.println("=== 收到学生排行榜请求 ===");
        List<Map<String, Object>> result = statisticsService.getMonthlyStudentRanking();
        System.out.println("=== 返回学生排行榜数据：" + result + " ===");
        return result;
    }

    @GetMapping("/books/ranking")
    public List<Map<String, Object>> getBookRank() {
        System.out.println("=== 收到图书排行榜请求 ===");
        List<Map<String, Object>> result = statisticsService.getMonthlyBookRanking();
        System.out.println("=== 返回图书排行榜数据：" + result + " ===");
        return result;
    }
}