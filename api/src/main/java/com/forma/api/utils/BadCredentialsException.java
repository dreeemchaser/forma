package com.forma.api.utils;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super("Bad credentials: " + message);
    }
}
