package com.hakitahu.bookstore.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/books")
public class BookController {

    @GetMapping
    public List<String> getAllBooks() {
        return Arrays.asList("Đắc Nhân Tâm", "Nhà Giả Kim");
    }
}