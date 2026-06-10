package com.example.libbookmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "libcard")
@Data
public class LibCard {
    @Id
    @Column(length = 5)
    private String sno;

    @Column(name = "cardNo", length = 4)
    private String cardNo;

    @Column(length = 20)
    private String password;

    @Column(length = 30)
    private String email;

    @Column(length = 2)
    private String status; // 正常/挂失/注销

    // 恢复学生关联关系
    @ManyToOne
    @JoinColumn(name = "sno", insertable = false, updatable = false)
    @JsonIgnore // 防止JSON序列化循环引用
    private Student student;
}