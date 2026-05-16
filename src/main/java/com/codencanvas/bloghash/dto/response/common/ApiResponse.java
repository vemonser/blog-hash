package com.codencanvas.bloghash.dto.response.common;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // ← الجديد
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
