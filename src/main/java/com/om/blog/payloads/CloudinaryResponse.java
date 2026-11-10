package com.om.blog.payloads;

public record CloudinaryResponse(
        String publicId,
        String secureUrl,
        String format,
        long bytes
) {
}

