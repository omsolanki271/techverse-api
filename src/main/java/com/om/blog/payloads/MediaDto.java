package com.om.blog.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class MediaDto {

    private Integer mediaId;

    @NotBlank(message = "FileName is required")
    private String fileName;

    @NotBlank(message = "FileType is required")
    private String fileType;

    private String filePath;

    private LocalDateTime uploadedDate;

}
