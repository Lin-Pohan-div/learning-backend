package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.ReviewRequest;
import com.learning.api.entity.Reviews;
import com.learning.api.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControlTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    }

    @Test
    void getById_existingId_shouldReturn200WithReview() throws Exception {
        Reviews review = new Reviews();
        review.setId(1L);
        review.setUserId(10L);
        review.setCourseId(20L);
        review.setFocusScore(4);
        review.setComprehensionScore(3);
        review.setConfidenceScore(5);
        review.setComment("Initial comment");

        when(reviewService.findById(1L)).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/reviews/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.courseId").value(20));
    }

    @Test
    void getAverageRating_shouldReturnCourseIdAndAverageRating() throws Exception {
        when(reviewService.getAverageRating(20L)).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/course/{courseId}/average-rating", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(20))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void post_validRequest_shouldReturn201WithCreatedReview() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(10L);
        request.setCourseId(21L);
        request.setFocusScore(5);
        request.setComprehensionScore(4);
        request.setConfidenceScore(3);
        request.setComment("Excellent session");

        Reviews saved = new Reviews();
        saved.setId(5L);
        saved.setUserId(10L);
        saved.setCourseId(21L);
        saved.setFocusScore(5);
        saved.setComprehensionScore(4);
        saved.setConfidenceScore(3);
        saved.setComment("Excellent session");

        when(reviewService.save(any(Reviews.class))).thenReturn(saved);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.courseId").value(21));
    }

    @Test
    void post_missingUserId_shouldReturn400() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setCourseId(21L);
        request.setFocusScore(5);
        request.setComprehensionScore(4);
        request.setConfidenceScore(3);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("userId")));
    }

    @Test
    void post_missingCourseId_shouldReturn400() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(10L);
        request.setFocusScore(5);
        request.setComprehensionScore(4);
        request.setConfidenceScore(3);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("courseId")));
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        when(reviewService.deleteById(eq(999L))).thenReturn(false);

        mockMvc.perform(delete("/api/reviews/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
