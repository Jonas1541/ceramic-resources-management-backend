package com.jonasdurau.ceramicmanagement.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
