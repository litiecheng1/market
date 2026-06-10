package com.example.libbookmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "book")
@Data
public class Book {
    @Id
    @Column(name = "ISBN", length = 6)
    private String isbn;

    @Column(name = "bName", length = 20)
    private String bName;

    @Column(length = 20)
    private String author;

    @Column(length = 30)
    private String publisher;

    @Column(name = "pubDate")
    private LocalDate pubDate;
}