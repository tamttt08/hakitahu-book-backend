package com.hakitahu.bookstore.repository;

import com.hakitahu.bookstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Tạm thời chưa cần viết thêm hàm gì, JPA đã có sẵn hàm findAll()
}