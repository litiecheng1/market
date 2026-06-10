package com.example.libbookmanagement.service;

import com.example.libbookmanagement.entity.Book;
import com.example.libbookmanagement.entity.BookCopy;
import com.example.libbookmanagement.repository.BookCopyRepository;
import com.example.libbookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    // 图书搜索（匹配修复后的Repository方法）
    public List<Book> searchBooks(String keyword, String type) {
        switch (type) {
            case "bName":
                return bookRepository.findByBNameContaining(keyword);
            case "author":
                return bookRepository.findByAuthorContaining(keyword);
            case "publisher":
                return bookRepository.findByPublisherContaining(keyword);
            case "ISBN":
                // 调用修复后的方法
                return bookRepository.findByIsbnContaining(keyword);
            default:
                return bookRepository.findAll();
        }
    }

    // 获取图书副本
    public List<BookCopy> getBookCopiesByIsbn(String isbn) {
        return bookCopyRepository.findByBookISBN(isbn);
    }
}