package com.niyiment.authservice.exceptions;

/**
 * Base exception for authentication service.
 */
public class AuthServiceException extends RuntimeException {
    
    public AuthServiceException(String message) {
        super(message);
    }

    public AuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}