package com.learning.api.dto;
public record PackageResponseDTO(
	    Long orderId,
	    String courseName,
	    Integer totalLessons,
	    Integer usedLessons,
	    Integer remainingLessons,
	    Integer status
	) {}