package com.om.blog.services.impl;

import com.om.blog.entities.Category;
import com.om.blog.exceptions.ResourceNotFoundException;
import com.om.blog.payloads.CategoryDto;
import com.om.blog.repositories.CategoryRepo;
import com.om.blog.services.CategoryService;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category =  this.modelMapper.map(categoryDto, Category.class);
        Category saved =  this.categoryRepo.save(category);
        return this.modelMapper.map(saved,CategoryDto.class);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Integer categoryId) {
        Category category =   this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category ","Category Id" , categoryId));

        // only two value pass so here not use modelmapper
        category.setCategoryTitle(categoryDto.getCategoryTitle());
        category.setCategoryDescription(categoryDto.getCategoryDescription());

        Category category1 =  categoryRepo.save(category);
        return this.modelMapper.map(category1 , CategoryDto.class);
    }

    @Override
    public void deleteCategory(Integer categoryId) {

    }

    @Override
    public CategoryDto getCategory(CategoryDto categoryDto, Integer categoryId) {
        return null;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return List.of();
    }
}
