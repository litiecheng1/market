package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.Book;
import com.example.libbookmanagement.entity.BookCopy;
import com.example.libbookmanagement.repository.BookCopyRepository;
import com.example.libbookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    // 图书搜索
    public List<Book> searchBooks(String keyword, String type) {
        switch (type) {
            case "bName":
                return bookRepository.findByBNameContaining(keyword);
            case "author":
                return bookRepository.findByAuthorContaining(keyword);
            case "publisher":
                return bookRepository.findByPublisherContaining(keyword);
            case "isbn":
                return bookRepository.findByIsbnContaining(keyword);
            default:
                return bookRepository.findAll();
        }
    }

    // 获取图书副本
    public List<BookCopy> getBookCopiesByIsbn(String isbn) {
        return bookCopyRepository.findByBookISBN(isbn);
    }

    // ========== 新增方法 ==========

    /**
     * 添加新书
     */
    @Transactional
    public String addBook(Book book) {
        // 检查ISBN是否已存在
        if (bookRepository.existsById(book.getIsbn())) {
            return "添加失败，该ISBN已存在";
        }
        // 校验必填字段
        if (book.getIsbn() == null || book.getIsbn().isEmpty() ||
                book.getBName() == null || book.getBName().isEmpty() ||
                book.getAuthor() == null || book.getAuthor().isEmpty() ||
                book.getPublisher() == null || book.getPublisher().isEmpty()) {
            return "添加失败，请填写完整信息";
        }
        bookRepository.save(book);
        return "新书添加成功";
    }

    /**
     * 添加图书副本
     */
    // BookService.java - 修复 addBookCopy 方法
    @Transactional
    public String addBookCopy(BookCopy bookCopy) {
        // 1. 检查条形码是否为空
        if (bookCopy.getBarCode() == null || bookCopy.getBarCode().isEmpty()) {
            return "添加失败：条形码不能为空";
        }

        // 2. 检查ISBN是否为空
        if (bookCopy.getBookISBN() == null || bookCopy.getBookISBN().isEmpty()) {
            return "添加失败：图书ISBN不能为空";
        }

        // 3. 检查图书是否存在
        if (!bookRepository.existsById(bookCopy.getBookISBN())) {
            return "添加失败：图书不存在，ISBN: " + bookCopy.getBookISBN();
        }

        // 4. 检查条形码是否已存在
        if (bookCopyRepository.existsById(bookCopy.getBarCode())) {
            return "添加失败：条形码 " + bookCopy.getBarCode() + " 已存在";
        }

        // 5. 设置默认状态
        if (bookCopy.getStatus() == null || bookCopy.getStatus().isEmpty()) {
            bookCopy.setStatus("可借");
        }

        // 6. 设置默认藏书位置
        if (bookCopy.getPlace() == null || bookCopy.getPlace().isEmpty()) {
            bookCopy.setPlace("待分配");
        }

        try {
            bookCopyRepository.save(bookCopy);
            return "添加成功：条形码 " + bookCopy.getBarCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "添加失败：" + e.getMessage();
        }
    }

    /**
     * 报废图书副本（逻辑删除，将状态改为"报废"）
     */
    @Transactional
    public String scrapBookCopy(String barcode) {
        Optional<BookCopy> optional = bookCopyRepository.findById(barcode);
        if (optional.isEmpty()) {
            return "报废失败，图书副本不存在";
        }
        BookCopy copy = optional.get();
        // 检查是否已借出
        if ("借出".equals(copy.getStatus())) {
            return "报废失败，该副本已被借出，无法报废";
        }
        copy.setStatus("报废");
        bookCopyRepository.save(copy);
        return "图书副本报废成功";
    }

    /**
     * 图书下架（删除该图书的所有副本）
     */
    @Transactional
    public String takeDownBook(String isbn) {
        // 检查图书是否存在
        if (!bookRepository.existsById(isbn)) {
            return "下架失败，图书不存在";
        }
        // 查询该图书的所有副本
        List<BookCopy> copies = bookCopyRepository.findByBookISBN(isbn);
        if (copies.isEmpty()) {
            return "下架失败，该图书没有副本";
        }
        // 检查是否有未归还的副本
        for (BookCopy copy : copies) {
            if ("借出".equals(copy.getStatus())) {
                return "下架失败，存在被借出的副本，请先归还";
            }
        }
        // 删除所有副本（物理删除）
        bookCopyRepository.deleteAll(copies);
        // 可选：是否删除图书主记录？根据需求决定。这里不删除主记录，只删除副本。
        return "图书下架成功，已移除 " + copies.size() + " 个副本";
    }
    // 获取所有图书
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // 更新图书信息
    @Transactional
    public String updateBook(String isbn, Book book) {
        Optional<Book> existingOpt = bookRepository.findById(isbn);
        if (existingOpt.isEmpty()) {
            return "更新失败，图书不存在";
        }

        Book existing = existingOpt.get();
        if (book.getBName() != null && !book.getBName().isEmpty()) {
            existing.setBName(book.getBName());
        }
        if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
            existing.setAuthor(book.getAuthor());
        }
        if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
            existing.setPublisher(book.getPublisher());
        }
        if (book.getPubDate() != null) {
            existing.setPubDate(book.getPubDate());
        }

        bookRepository.save(existing);
        return "图书信息更新成功";
    }

}