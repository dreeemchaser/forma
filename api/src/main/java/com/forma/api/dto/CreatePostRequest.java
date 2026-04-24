package com.forma.api.dto;

import lombok.Builder;

@Builder
public record CreatePostRequest(String title, String body) {}
