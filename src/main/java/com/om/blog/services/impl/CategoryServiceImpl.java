package com.om.blog.services.impl;

import com.om.blog.payloads.CategoryDto;
import com.om.blog.services.CategoryService;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        return null;
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId) {
        return null;
    }

    @Override
    public void deleteCategory(Integer categoryId) {

    }

    @Override
    public CategoryDto getCategory(CategoryDto categoryDto, Integer categoryId) {
        return null;
    }

    @Override
    public List<CategoryDto> getAllCategory() {
        return List.of();
    }
}
