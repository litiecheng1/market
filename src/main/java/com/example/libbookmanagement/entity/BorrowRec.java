package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowrec")
@Data
public class BorrowRec {
    @EmbeddedId
    private BorrowRecId id;

    @Column(name = "borDate")
    private LocalDateTime borDate;

    @Column(name = "retDate")
    private LocalDateTime retDate;

    @Column(name = "realRetDate")
    private LocalDateTime realRetDate;

    @Column(name = "status")
    private String status;

    // ✅ 必须保留这两个getter，前端才能直接获取sno和barCode
    public String getSno() {
        return id.getSno();
    }

    public String getBarCode() {
        return id.getBarCode();
    }
}