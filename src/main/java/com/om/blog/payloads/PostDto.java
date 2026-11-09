package com.om.blog.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@JsonPropertyOrder({
        "postId",
        "title",
        "content",
        "imageName",
        "mediaId",
        "addedDate",
        "views",
        "shareCount",
        "status",
        "likeCount",
        "bookmarkCount",
        "liked",
        "bookmarked",
        "category",
        "user"
})
@Getter
@Setter
@NoArgsConstructor
public class PostDto {

    private Integer postId;

    @NotBlank(message = "Post title is required")
    @Size(min = 4, max = 100,
            message = "Title must be between 4 and 100 characters")
    private String title;

    @NotBlank(message = "Post content is required")
    @Size(min = 4, max = 10000,
            message = "Content must be between 4 and 10000 characters")
    private String content;

    private String imageName;

    private Integer mediaId;

    private LocalDateTime addedDate;

    private int views;

    private int shareCount = 0;

    private String status = "PUBLISHED"; // DRAFT, PUBLISHED

    private long likeCount = 0;

    private long bookmarkCount = 0;

    private boolean liked = false;

    private boolean bookmarked = false;

    private CategoryDto category;

    @JsonIgnoreProperties({"password", "email", "mobileNumber", "address", "roles", "createdAt", "updatedAt"})
    private UserDto user;
}
