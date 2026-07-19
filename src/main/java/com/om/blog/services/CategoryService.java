package com.om.blog.services;

import com.om.blog.payloads.CategoryDto;

import java.util.List;

public interface CategoryService {
    //create
    public CategoryDto createCategory(CategoryDto categoryDto);

    //update
    public  CategoryDto updateCategory(CategoryDto categoryDto , Integer categoryId);

    //delete
    public void deleteCategory(Integer categoryId);

    //get
    public CategoryDto getCategory(CategoryDto categoryDto , Integer categoryId);

    //all category get
    public List<CategoryDto> getAllCategory();

}
