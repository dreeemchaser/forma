package com.forma.api.utils;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super("Post not found: " + message);
    }
}
