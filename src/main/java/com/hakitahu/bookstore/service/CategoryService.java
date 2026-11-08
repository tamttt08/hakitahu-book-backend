package com.hakitahu.bookstore.service;

import com.hakitahu.bookstore.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(Category category);
    
    Category updateCategory(Integer id, Category categoryDetails);
    void deleteCategory(Integer id);
}