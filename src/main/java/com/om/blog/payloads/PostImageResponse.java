package com.om.blog.payloads;

public record PostImageResponse(
        byte[] imageData,
        String contentType
)
{
}