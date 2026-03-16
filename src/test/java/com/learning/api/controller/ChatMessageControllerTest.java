package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.entity.ChatMessage;
import com.learning.api.service.ChatMessageService;
import com.learning.api.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatMessageController).build();
    }

    @Test
    void getByBookingId_existingBooking_shouldReturnMessages() throws Exception {
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setOrderId(100L);
        msg.setRole(1);
        msg.setMessageType(1);
        msg.setMessage("hello");

        when(chatMessageService.findByBookingId(100L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chatMessage/booking/{bookingId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(100))
                .andExpect(jsonPath("$[0].message").value("hello"));
    }

    @Test
    void post_validRequest_shouldReturn201() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setBookingId(100L);
        request.setRole(1);
        request.setMessageType(1);
        request.setMessage("hi");

        ChatMessage saved = new ChatMessage();
        saved.setId(9L);
        saved.setOrderId(100L);
        saved.setRole(1);
        saved.setMessageType(1);
        saved.setMessage("hi");

        when(chatMessageService.save(eq(100L), eq(1), eq(1), eq("hi"), any())).thenReturn(saved);

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void post_missingBookingId_shouldReturn400() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRole(1);
        request.setMessageType(1);
        request.setMessage("hi");

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Booking ID")));
    }

    @Test
    void post_nonExistingBookingId_shouldReturn404() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setBookingId(999L);
        request.setRole(1);
        request.setMessageType(1);
        request.setMessage("hi");

        when(chatMessageService.save(anyLong(), eq(1), eq(1), eq("hi"), any()))
                .thenThrow(new NoSuchElementException("Booking not found"));

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Booking not found")));
    }

    @Test
    void upload_emptyFile_shouldReturn400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/chatMessage/upload")
                        .file(file)
                        .param("bookingId", "100")
                        .param("role", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("檔案不能為空")));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        when(chatMessageService.update(eq(404L), any())).thenReturn(Optional.empty());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/chatMessage/{id}", 404L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"update\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        when(chatMessageService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/chatMessage/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
