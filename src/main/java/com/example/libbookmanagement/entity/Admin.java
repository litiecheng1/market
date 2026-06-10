package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin")
@Data
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String username;

    @Column(length = 20)
    private String password;

    @Column(name = "adminType", nullable = false)
    private Integer adminType = 1; // 0:证件管理 1:采编 2:流通
}