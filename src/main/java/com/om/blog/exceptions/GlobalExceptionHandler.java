package com.om.blog.exceptions;

import com.om.blog.payloads.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler{

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
    public ResponseEntity<ApiResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex)
    {
        String msg = "Validation failed";

        if (ex.getBindingResult().getFieldError() != null) {
            msg = ex.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }
        ApiResponse response = new ApiResponse(msg , false);
        return  new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
}
