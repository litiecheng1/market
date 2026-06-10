package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin(origins = "*")
public class BorrowController {
    @Autowired
    private BorrowService borrowService;

    @PostMapping
    public ResponseEntity<String> borrowBooks(
            @RequestParam String sno,
            @RequestBody List<String> barCodes) {
        return ResponseEntity.ok(borrowService.borrowBooks(sno, barCodes));
    }

    @PostMapping("/return/{barCode}")
    public ResponseEntity<String> returnBook(@PathVariable String barCode) {
        return ResponseEntity.ok(borrowService.returnBook(barCode));
    }
}