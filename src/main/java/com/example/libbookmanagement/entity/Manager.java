package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "manager")
@Data
public class Manager {
    @Id
    @Column(length = 10)
    private String mno;

    @Column(name = "mName", length = 20, nullable = false)
    private String mName;

    @Column(name = "mSex", length = 2)
    private String mSex = "男";

    @Column(name = "mPhone", length = 15, unique = true)
    private String mPhone;

    @Column(name = "hireDate")
    private LocalDate hireDate;
}