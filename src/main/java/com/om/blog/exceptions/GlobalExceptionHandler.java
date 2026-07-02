package com.om.blog.exceptions;

import com.om.blog.payloads.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    private final View error;

    public GlobalExceptionHandler(View error) {
        this.error = error;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundExceptionHandler(ResourceNotFoundException ex)
    {
        String msg = ex.getMessage();
        ApiResponse response = new ApiResponse(msg,false);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Invalid Path Variable Exception

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex)
    {
        String msg = String.format("Invalid value '%s' for '%s'. Please enter a valid number.",
                ex.getValue(),
                ex.getName());
        ApiResponse response = new ApiResponse(msg, false);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> methodArgumentNotValidException(MethodArgumentNotValidException ex)
    {
        Map<String,String> response = new HashMap<>();

        for(FieldError error : ex.getBindingResult().getFieldErrors())
        {
            String fieldName= error.getField();
            String message = error.getDefaultMessage();
            response.put(fieldName,message);
        }

        return  new ResponseEntity<Map<String,String>>(response,HttpStatus.BAD_REQUEST);
    }
}
