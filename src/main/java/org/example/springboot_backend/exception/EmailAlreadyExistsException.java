package org.example.springboot_backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}