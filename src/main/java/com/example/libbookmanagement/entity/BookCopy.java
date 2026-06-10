package com.example.libbookmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bookcopy")
@Data
public class BookCopy {
    @Id
    @Column(name = "barCode", length = 8)
    private String barCode;

    @Column(name = "bookISBN", length = 6)
    private String bookISBN;

    @Column(length = 20)
    private String place;

    @Column(length = 2)
    private String status; // 可借/库本/借出

    // 恢复图书关联关系
    @ManyToOne
    @JoinColumn(name = "bookISBN", insertable = false, updatable = false)
    @JsonIgnore
    private Book book;
}