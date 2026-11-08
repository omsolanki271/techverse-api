package com.om.blog.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentDto {

    private int id;

    @NotBlank(message = "Comment content is required")
    @Size(min = 2, max = 1000, message = "Comment must be between 2 and 1000 characters")
    private String content;

    @JsonIgnoreProperties({"password", "email", "mobileNumber", "address", "roles", "createdAt", "updatedAt"})
    private UserDto user;

}
