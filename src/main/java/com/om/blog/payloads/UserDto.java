package com.om.blog.payloads;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.om.blog.entities.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(min = 4,message = "Name must be at least 4 characters long")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Size(max = 500, message = "About cannot exceed 500 characters")
    private String about;

    private String mobileNumber;
    private String address;
    private String githubUrl;
    private String linkedinUrl;
    private String instaUrl;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String googleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<Role> roles;
}
