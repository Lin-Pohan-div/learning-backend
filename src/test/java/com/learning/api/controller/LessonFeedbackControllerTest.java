package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.feedback.FeedbackRequest;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.exception.GlobalExceptionHandler;
import com.learning.api.service.LessonFeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonFeedbackControllerTest {

    @Mock
    private LessonFeedbackService lessonFeedbackService;

    @InjectMocks
    private FeedbackController feedbackController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAll_shouldReturnListWithSavedFeedback() throws Exception {
        LessonFeedback feedback = new LessonFeedback();
        feedback.setId(1L);
        feedback.setBookingId(101L);
        feedback.setRating(4);
        feedback.setComment("Initial feedback");

        when(lessonFeedbackService.findAll()).thenReturn(List.of(feedback));

        mockMvc.perform(get("/api/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rating").value(4));
    }

    @Test
    void getById_nonExistingId_shouldReturn404() throws Exception {
        when(lessonFeedbackService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/feedbacks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_validRequest_shouldReturn201() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setBookingId(101L);
        request.setFocusScore(5);
        request.setComprehensionScore(4);
        request.setConfidenceScore(3);
        request.setRating(5);
        request.setComment("Great lesson");

        LessonFeedback saved = new LessonFeedback();
        saved.setId(7L);
        saved.setBookingId(101L);
        saved.setFocusScore(5);
        saved.setComprehensionScore(4);
        saved.setConfidenceScore(3);
        saved.setRating(5);
        saved.setComment("Great lesson");

        when(lessonFeedbackService.save(any(LessonFeedback.class))).thenReturn(saved);

        mockMvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.bookingId").value(101))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void post_invalidRequest_shouldReturn400() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setBookingId(101L);
        request.setFocusScore(0);
        request.setComprehensionScore(4);
        request.setConfidenceScore(3);
        request.setRating(5);

        when(lessonFeedbackService.save(any(LessonFeedback.class)))
                .thenThrow(new IllegalArgumentException("專注度必須在 1 到 5 之間"));

        mockMvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg", containsString("專注度")));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setBookingId(101L);
        request.setFocusScore(2);
        request.setComprehensionScore(2);
        request.setConfidenceScore(2);
        request.setRating(2);

        when(lessonFeedbackService.update(eq(999L), any(LessonFeedback.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/feedbacks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        when(lessonFeedbackService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/feedbacks/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
