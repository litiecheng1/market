package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {
    // 按图书ISBN查询所有副本
    List<BookCopy> findByBookISBN(String bookISBN);
}