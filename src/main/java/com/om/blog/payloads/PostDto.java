package com.om.blog.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer postId;

    @NotBlank(message = "Post title is required")
    @Size(min = 4, max = 100,
            message = "Title must be between 4 and 100 characters")
    private String title;

    @NotBlank(message = "Post content is required")
    @Size(min = 4, max = 10000,
            message = "Content must be between 4 and 10000 characters")
    private String content;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String cloudinaryUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String publicId;

    private Integer mediaId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime addedDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int views;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int shareCount = 0;

    private String status = "PUBLISHED"; // DRAFT, PUBLISHED

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long likeCount = 0L;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long bookmarkCount = 0L;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean liked = false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean bookmarked = false;

    private CategoryDto category;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnoreProperties({"password", "email", "mobileNumber", "address", "roles", "createdAt", "updatedAt"})
    private UserDto user;
}
