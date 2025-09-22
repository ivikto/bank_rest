package com.example.bankcards.exception;

public class TransferAccessException extends RuntimeException {
    public TransferAccessException(String message) {
        super(message);
    }
}
