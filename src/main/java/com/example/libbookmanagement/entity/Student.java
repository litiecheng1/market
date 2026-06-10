package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student")
@Data
public class Student {
    @Id
    @Column(length = 5)
    private String sno;

    @Column(name = "sName", length = 20)
    private String sName;

    @Column(name = "sSex", length = 1)
    private String sSex;

    @Column(length = 3)
    private String type; // 研究生/本科生/专科生

    @Column(length = 20)
    private String college;

    @Column(length = 20)
    private String major;

    @Column(name = "sDorm", length = 20)
    private String sDorm;

    @Column(name = "sAge")
    private Integer sAge;

    @Column(name = "originPlace", length = 20)
    private String originPlace;
}