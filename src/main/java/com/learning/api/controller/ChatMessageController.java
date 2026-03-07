package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.entity.ChatMessage;
import com.learning.api.service.ChatMessageService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chat-messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ChatMessage>> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(chatMessageService.findByBookingId(bookingId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChatMessageRequest request) {
        try {
            if (request.getBookingId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: Booking ID 不能為空"));
            }

            if (request.getRole() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: Role 不能為空"));
            }

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: 消息內容不能為空"));
            }

            ChatMessage chatMessage = chatMessageService.save(
                request.getBookingId(),
                request.getRole(),
                request.getMessage()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(chatMessage);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("錯誤: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String message = body.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: 消息內容不能為空"));
            }
            return chatMessageService.update(id, message)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return chatMessageService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
