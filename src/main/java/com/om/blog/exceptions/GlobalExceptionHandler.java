package com.om.blog.exceptions;

import com.om.blog.payloads.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> resourceNotFoundExceptionHandler(ResourceNotFoundException ex)
    {
        String msg = ex.getMessage();
        ApiResponse response = new ApiResponse(msg,false);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyInUseException.class)
    public ResponseEntity<ApiResponse> resourceAlreadyInUseException(
            ResourceAlreadyInUseException ex) {
        String msg = ex.getMessage();
        ApiResponse response = new ApiResponse(msg,false);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiResponse> badCredentialsExceptionHandler(org.springframework.security.authentication.BadCredentialsException ex)
    {
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank()) ? ex.getMessage() : "Invalid Username or Password !!";
        ApiResponse response = new ApiResponse(msg, false);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String msg = "Malformed JSON request or invalid data format.";
        if (ex.getMessage() != null && ex.getMessage().contains("Cannot map null into type long")) {
            msg = "Invalid numeric field value: cannot map null into primitive number.";
        } else if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            msg = ex.getMostSpecificCause().getMessage();
        }
        return new ResponseEntity<>(new ApiResponse(msg, false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        String msg = "Database constraint violation. An account with this email or mobile number may already exist.";
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
            msg = "Email address is already registered. Please sign in instead.";
        } else if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("mobile")) {
            msg = "Mobile number is already registered.";
        }
        return new ResponseEntity<>(new ApiResponse(msg, false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank()) ? ex.getMessage() : "An unexpected server error occurred.";
        return new ResponseEntity<>(new ApiResponse(msg, false), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
