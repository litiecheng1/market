package com.example.libbookmanagement.repository;

import com.example.libbookmanagement.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    // 按书名模糊查询（手动写JPQL，完全匹配实体字段）
    @Query("SELECT b FROM Book b WHERE b.bName LIKE CONCAT('%', :keyword, '%')")
    List<Book> findByBNameContaining(@Param("keyword") String keyword);

    // 按作者模糊查询
    @Query("SELECT b FROM Book b WHERE b.author LIKE CONCAT('%', :keyword, '%')")
    List<Book> findByAuthorContaining(@Param("keyword") String keyword);

    // 按出版社模糊查询
    @Query("SELECT b FROM Book b WHERE b.publisher LIKE CONCAT('%', :keyword, '%')")
    List<Book> findByPublisherContaining(@Param("keyword") String keyword);

    // 按ISBN模糊查询
    @Query("SELECT b FROM Book b WHERE b.isbn LIKE CONCAT('%', :keyword, '%')")
    List<Book> findByIsbnContaining(@Param("keyword") String keyword);
}