package com.om.blog.payloads;

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

    private String title;
    private String content;
    private String imageName;
    private LocalDateTime addedDate;
}
