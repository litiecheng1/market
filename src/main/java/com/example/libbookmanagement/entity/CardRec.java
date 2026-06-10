package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "cardrec")
@Data
public class CardRec {
    @Id
    @Column(name = "serNum", length = 5)
    private String serNum;

    @Column(length = 5)
    private String sno;

    @Column(name = "originCardNo", length = 4)
    private String originCardNo;

    @Column(name = "newCardNo", length = 4)
    private String newCardNo;

    @Column(name = "opType", length = 2)
    private String opType; // 新办/挂失/补办/注销

    @Column(name = "opTime")
    private LocalDateTime opTime;
}