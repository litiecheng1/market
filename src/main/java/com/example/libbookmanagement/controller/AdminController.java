package com.example.libbookmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名和密码不能为空");
        }

        // 直接执行原生SQL，完全匹配数据库字段名
        String sql = "SELECT id, username, adminType FROM admin WHERE username = ? AND password = ?";

        try {
            Map<String, Object> admin = jdbcTemplate.queryForMap(sql, username, password);

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", admin.get("id"));
            result.put("username", admin.get("username"));
            result.put("adminType", admin.get("adminType"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 查询不到结果时会抛出异常
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
    }
}