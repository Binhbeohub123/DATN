package com.polycinema.backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler — wraps all unhandled exceptions into a consistent
 * JSON error response: { success, message, timestamp }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 Bad Request ──────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 404 Not Found ────────────────────────────────────────────
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            jakarta.persistence.EntityNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 403 Forbidden ────────────────────────────────────────────
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    // ── 409 Conflict — DB unique-constraint violation ─────────────
    // Catches DataIntegrityViolationException thrown when a UNIQUE KEY is violated.
    // The UC_NoOverlap constraint on (PhongChieuId, ThoiGianBatDau) is the primary
    // source: this happens when a soft-deleted row occupies the same slot (the DB
    // constraint is not filtered on IsDeleted). The filtered unique index migration
    // (migration_filtered_unique.sql) is the permanent fix; this handler is the
    // safety net for any constraint violation that slips past application checks.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        String raw = ex.getMostSpecificCause().getMessage();
        String msg;
        if (raw != null && raw.contains("UC_NoOverlap")) {
            msg = "Phòng chiếu đã có suất chiếu trong khung giờ này (trùng lịch). Vui lòng chọn giờ khác.";
        } else if (raw != null && raw.contains("UNIQUE") || (raw != null && raw.contains("duplicate"))) {
            msg = "Dữ liệu bị trùng lặp. Vui lòng kiểm tra lại thông tin đã nhập.";
        } else {
            msg = "Vi phạm ràng buộc dữ liệu. Vui lòng kiểm tra lại.";
        }
        System.err.println("[GlobalExceptionHandler] DataIntegrityViolation: " + raw);
        return build(HttpStatus.CONFLICT, msg);
    }

    // ── 500 Internal Server Error ─────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(
            Exception ex, WebRequest request) {
        // Log the real exception for debugging — do not expose internals to client
        System.err.println("[GlobalExceptionHandler] Unhandled exception: " + ex.getClass().getName() + " — " + ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi server. Vui lòng thử lại sau.");
    }

    // ── Helper ───────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message != null ? message : status.getReasonPhrase());
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
