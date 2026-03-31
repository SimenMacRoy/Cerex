package com.cerex.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard API response envelope used for ALL endpoints.
 *
 * <p>Follows the Cerex API standard:
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "Operation successful",
 *   "timestamp": "2026-03-30T12:00:00Z"
 * }
 * </pre>
 *
 * @param <T> the type of the response data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private PaginationInfo pagination;

    // ── Factory Methods ─────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> ok(T data, PaginationInfo pagination) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .pagination(pagination)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, T errorDetails) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(errorDetails)
            .timestamp(Instant.now())
            .build();
    }

    /** Alias for {@link #ok(Object)} — preferred by controller layer. */
    public static <T> ApiResponse<T> success(T data) {
        return ok(data);
    }

    /** Alias for {@link #ok(Object, String)} — preferred by controller layer. */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ok(data, message);
    }

    /** Alias for {@link #ok(Object, PaginationInfo)} — preferred by controller layer. */
    public static <T> ApiResponse<T> success(T data, PaginationInfo pagination) {
        return ok(data, pagination);
    }

    /**
     * Pagination metadata for list endpoints.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
