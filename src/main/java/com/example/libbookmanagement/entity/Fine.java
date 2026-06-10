package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine")
@Data
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 5, nullable = false)
    private String sno;

    @Column(length = 8, nullable = false)
    private String barCode;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "overdueDays", nullable = false)
    private Integer overdueDays;

    @Column(name = "fineDate", nullable = false)
    private LocalDateTime fineDate;

    @Column(name = "paidDate")
    private LocalDateTime paidDate;

    @Column(length = 10, nullable = false)
    private String status; // 未缴纳/已缴纳
}