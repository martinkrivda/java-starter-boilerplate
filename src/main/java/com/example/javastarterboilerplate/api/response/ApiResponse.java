package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
@Schema(name = "ApiResponse", description = "Standard JSON response envelope for REST API responses.")
public record ApiResponse<T>(@Schema(description = "True only for 2xx responses.") boolean success,
        @Schema(description = "Successful payload. Null for error responses.") T data,
        @Schema(description = "Problem details payload. Null for successful responses.") ApiProblemDetails error,
        @Schema(description = "Response metadata.") ResponseMeta meta) {

    public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> failure(ApiProblemDetails error, ResponseMeta meta) {
        return new ApiResponse<>(false, null, error, meta);
    }
}
