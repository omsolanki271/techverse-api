package com.om.blog.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class CategoryDto {

    private Integer categoryId;
    @NotBlank(message = "Category title is required.")
    @Size(min = 3, max = 100, message = "Category title must be between 3 and 100 characters.")
    private String categoryTitle;

    @NotBlank(message = "Category description is required.")
    @Size(min = 10, max = 500, message = "Category description must be between 10 and 500 characters.")
    private String categoryDescription;

}
