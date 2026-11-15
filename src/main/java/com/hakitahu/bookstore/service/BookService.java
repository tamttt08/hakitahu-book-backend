package com.hakitahu.bookstore.service;

import com.hakitahu.bookstore.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> getAllBooks();
    Book getBookById(Integer id);
    Book createBook(Book book);
    Book updateBook(Integer id, Book bookDetails);
    void deleteBook(Integer id);
}