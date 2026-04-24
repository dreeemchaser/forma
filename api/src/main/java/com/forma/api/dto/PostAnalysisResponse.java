package com.forma.api.dto;

public record PostAnalysisResponse(
        double score,
        String reasoning
) {}
