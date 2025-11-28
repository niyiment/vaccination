package com.niyiment.authservice.exceptions;

/**
 * Exception thrown when user already exists.
 */
public class UserAlreadyExistsException extends AuthServiceException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when user is not found.
 */
class UserNotFoundException extends AuthServiceException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when user account is locked.
 */
class AccountLockedException extends AuthServiceException {
    
    public AccountLockedException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when refresh token is invalid or expired.
 */
class InvalidRefreshTokenException extends AuthServiceException {
    
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when authentication credentials are invalid.
 */
class InvalidCredentialsException extends AuthServiceException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}