package com.example.bankcards.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
    public DuplicateUserException(String message,
                                  DataIntegrityViolationException ex) {
        super(message, ex);
    }
}
