package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.service.ChatMessageService;
import com.learning.api.service.FileStorageService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chatMessage")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final FileStorageService fileStorageService;

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ChatMessage>> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(chatMessageService.findByBookingId(bookingId));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookingId") Long bookingId,
            @RequestParam("role") String role,
            @RequestParam(value = "message", required = false) String message) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("檔案不能為空"));
            }
            String mediaUrl = fileStorageService.store(file);
            int messageType = fileStorageService.detectMessageType(file);
            String originalFileName = file.getOriginalFilename();
            String textMessage = (message != null && !message.isBlank()) ? message : originalFileName;

            ChatMessage chatMessage = chatMessageService.save(
                    bookingId, role, messageType, textMessage, mediaUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(chatMessage);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("錯誤: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("上傳失敗: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> download(
            @PathVariable String filename,
            @RequestParam(value = "name", required = false) String originalName) {
        try {
            Resource resource = fileStorageService.loadAsResource(filename);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String downloadName = (originalName != null && !originalName.isBlank())
                    ? originalName : filename;
            String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChatMessageRequest request) {
        try {
            String validationError = validateRequest(request);
            if (validationError != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: " + validationError));
            }

            ChatMessage chatMessage = chatMessageService.save(
                request.getBookingId(),
                request.getRole(),
                request.getMessageType(),
                request.getMessage(),
                request.getMediaUrl()
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

    private String validateRequest(ChatMessageRequest request) {
        if (request.getBookingId() == null) return "Booking ID 不能為空";
        if (request.getRole() == null) return "Role 不能為空";

        int typeValue = request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT.getValue();
        MessageType type;
        try {
            type = MessageType.fromValue(typeValue);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        if (type.isMedia()) {
            if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
                return typeName(type) + " URL 不能為空";
            }
        } else {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return "消息內容不能為空";
            }
        }

        return null;
    }

    private String typeName(MessageType type) {
        return switch (type) {
            case STICKER -> "貼圖";
            case VOICE -> "語音";
            case IMAGE -> "圖片";
            case VIDEO -> "影片";
            default -> "媒體";
        };
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
