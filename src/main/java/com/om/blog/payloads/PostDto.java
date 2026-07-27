package com.om.blog.payloads;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.om.blog.entities.Category;
import com.om.blog.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PostDto {

    private Integer postId;
    private String title;
    private String content;
    private String imageName;
    private LocalDateTime addedDate;
    private CategoryDto category;
    private UserDto user;
}
