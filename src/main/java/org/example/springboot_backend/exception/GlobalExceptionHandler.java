package org.example.springboot_backend.exception;

import org.example.springboot_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            "Invalid Credentials",
            HttpStatus.UNAUTHORIZED.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            "User Not Found",
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid email or password. Please check your credentials and try again.",
            "Authentication Failed",
            HttpStatus.UNAUTHORIZED.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "An unexpected error occurred. Please try again later.",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}