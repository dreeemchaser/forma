package com.forma.api.utils.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("User not found: " + message);
    }
}
