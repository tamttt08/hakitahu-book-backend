package com.hakitahu.bookstore.service.impl;

import com.hakitahu.bookstore.entity.Category;
import com.hakitahu.bookstore.repository.CategoryRepository;
import com.hakitahu.bookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // Lấy toàn bộ danh mục từ SQL Server
    }
    
    @Override
    public Category createCategory(Category category) {
        // SQL Server sẽ tự động tạo ID và gán Status = true (1) như cấu hình
        return categoryRepository.save(category);
    }
    
    @Override
    public Category updateCategory(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        category.setCategoryName(categoryDetails.getCategoryName());
        if (categoryDetails.getStatus() != null) {
            category.setStatus(categoryDetails.getStatus());
        }
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        // Xóa trực tiếp khỏi DB:
        categoryRepository.delete(category);
    }
}