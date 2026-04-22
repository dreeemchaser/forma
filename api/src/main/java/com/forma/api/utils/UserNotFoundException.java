package com.forma.api.utils;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super("User not found: " + message);
    }
}
