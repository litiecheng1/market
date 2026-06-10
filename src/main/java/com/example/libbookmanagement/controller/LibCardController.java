package com.example.libbookmanagement.controller;

import com.example.libbookmanagement.service.LibCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class LibCardController {
    @Autowired
    private LibCardService libCardService;

    @PostMapping("/create/{sno}")
    public ResponseEntity<String> createLibraryCard(@PathVariable String sno) {
        return ResponseEntity.ok(libCardService.createLibraryCard(sno));
    }

    @PutMapping("/lose/{sno}")
    public ResponseEntity<String> loseLibraryCard(@PathVariable String sno) {
        return ResponseEntity.ok(libCardService.loseLibraryCard(sno));
    }

    @PutMapping("/reissue/{sno}")
    public ResponseEntity<String> reissueLibraryCard(@PathVariable String sno) {
        return ResponseEntity.ok(libCardService.reissueLibraryCard(sno));
    }

    @PutMapping("/cancel/{sno}")
    public ResponseEntity<String> cancelLibraryCard(@PathVariable String sno) {
        return ResponseEntity.ok(libCardService.cancelLibraryCard(sno));
    }
}