package com.learning.api.dto;

public record CancelResponseDTO(boolean success, String message, Integer remainingLessons) {}
