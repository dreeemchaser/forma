package com.forma.api.utils.exceptions;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super("Post not found: " + message);
    }
}
