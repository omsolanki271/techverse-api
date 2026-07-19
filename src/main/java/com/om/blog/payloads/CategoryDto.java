package com.om.blog.payloads;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class CategoryDto {

    private Integer categoryId;
    private String categoryTitle;
    private String categoryDescription;

}
