package com.example.libbookmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 启用定时任务
public class LibBookManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibBookManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("图书馆管理系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}