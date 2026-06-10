package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.entity.Book;
import com.example.libbookmanagement.entity.BookCopy;
import com.example.libbookmanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    // 图书搜索接口（修复前端调用）
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam String keyword,
            @RequestParam String type) {
        List<Book> books = bookService.searchBooks(keyword, type);
        return ResponseEntity.ok(books);
    }

    // 获取图书副本
    @GetMapping("/{isbn}/copies")
    public ResponseEntity<List<BookCopy>> getBookCopies(@PathVariable String isbn) {
        List<BookCopy> copies = bookService.getBookCopiesByIsbn(isbn);
        return ResponseEntity.ok(copies);
    }
}