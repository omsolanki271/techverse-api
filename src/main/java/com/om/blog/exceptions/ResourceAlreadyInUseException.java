package com.om.blog.exceptions;

public class ResourceAlreadyInUseException extends RuntimeException {
    public ResourceAlreadyInUseException(String message) {
        super(message);
    }
}
