package com.learning.api.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/**
 * 標準化 API 回應格式
 * 讓前端能根據不同的錯誤狀態顯示對應的 UI
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of("msg", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralError(Exception e) {
        // 打印堆疊軌跡方便除錯
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("msg", "伺服器內部錯誤，請聯絡管理員"));
    }
}