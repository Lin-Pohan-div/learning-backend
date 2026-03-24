package com.learning.api.dto;

import java.time.LocalDate;

public record BookingResponseDTO(
	    String studentName,
	    Long bookingId,
	    String tutorName,
	    Integer subject,       
	    LocalDate date,
	    Integer hour,      
	    Integer status     
	) {}