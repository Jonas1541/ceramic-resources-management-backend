package com.jonasdurau.ceramicmanagement.shared.exception;

public class ResourceNotFoundException extends RuntimeException{
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
