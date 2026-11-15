package com.hakitahu.bookstore.service.impl;

import com.hakitahu.bookstore.entity.Book;
import com.hakitahu.bookstore.entity.Category;
import com.hakitahu.bookstore.repository.BookRepository;
import com.hakitahu.bookstore.repository.CategoryRepository;
import com.hakitahu.bookstore.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookServiceImpl(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Integer id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + id));
    }

    @Override
    public Book createBook(Book book) {
        if (book.getCategory() != null && book.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(book.getCategory().getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            book.setCategory(category);
        }
        if (book.getStatus() == null) {
            book.setStatus(true);
        }
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Integer id, Book bookDetails) {
        Book book = getBookById(id);
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setPrice(bookDetails.getPrice());
        book.setQuantity(bookDetails.getQuantity());
        book.setDescription(bookDetails.getDescription());
        book.setImage(bookDetails.getImage());
        if (bookDetails.getStatus() != null) {
            book.setStatus(bookDetails.getStatus());
        }
        if (bookDetails.getCategory() != null && bookDetails.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(bookDetails.getCategory().getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            book.setCategory(category);
        }
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Integer id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }
}