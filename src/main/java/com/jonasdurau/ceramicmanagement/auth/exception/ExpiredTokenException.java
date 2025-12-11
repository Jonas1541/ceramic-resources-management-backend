package com.jonasdurau.ceramicmanagement.auth.exception;

public class ExpiredTokenException extends RuntimeException {
    
    public ExpiredTokenException(String message) {
        super(message);
    }
}
