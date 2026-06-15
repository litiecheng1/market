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

    // 图书搜索接口
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

    // ========== 新增接口 ==========

    /**
     * 添加新书
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        System.out.println("=== 添加新书请求 ===");
        System.out.println("ISBN: " + book.getIsbn());
        System.out.println("书名: " + book.getBName());
        String result = bookService.addBook(book);
        return ResponseEntity.ok(result);
    }

    /**
     * 添加图书副本
     * POST /api/books/copies
     */
    @PostMapping("/copies")
    public ResponseEntity<String> addBookCopy(@RequestBody BookCopy bookCopy) {
        System.out.println("=== 添加图书副本请求 ===");
        System.out.println("条形码: " + bookCopy.getBarCode());
        System.out.println("所属ISBN: " + bookCopy.getBookISBN());
        String result = bookService.addBookCopy(bookCopy);
        return ResponseEntity.ok(result);
    }

    /**
     * 报废图书副本（单个副本）
     * PUT /api/books/copies/{barcode}/scrap
     */
    @PutMapping("/copies/{barcode}/scrap")
    public ResponseEntity<String> scrapBookCopy(@PathVariable String barcode) {
        System.out.println("=== 报废图书副本请求，条形码: " + barcode);
        String result = bookService.scrapBookCopy(barcode);
        return ResponseEntity.ok(result);
    }

    /**
     * 图书下架（删除该图书的所有副本）
     * PUT /api/books/{isbn}/takedown
     */
    @PutMapping("/{isbn}/takedown")
    public ResponseEntity<String> takeDownBook(@PathVariable String isbn) {
        System.out.println("=== 图书下架请求，ISBN: " + isbn);
        String result = bookService.takeDownBook(isbn);
        return ResponseEntity.ok(result);
    }
    // 获取所有图书
    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // 更新图书信息
    @PutMapping("/{isbn}")
    public ResponseEntity<String> updateBook(@PathVariable String isbn, @RequestBody Book book) {
        System.out.println("=== 更新图书请求，ISBN: " + isbn);
        String result = bookService.updateBook(isbn, book);
        return ResponseEntity.ok(result);
    }
}