package com.example.bankcards.exception;

public class IdenticalCardsException extends RuntimeException {
    public IdenticalCardsException(String message) {
        super(message);
    }
}
