package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard JSON response envelope for all REST API endpoints.
 *
 * <p>Every JSON response, success or error, is wrapped in this envelope. Use the static factory
 * methods {@link #success} and {@link #failure} to construct instances. Callers typically do not
 * construct this record directly; use {@link ApiResponseFactory} instead.
 *
 * <p>Health, metrics and OpenAPI document endpoints are deliberately excluded from this envelope
 * because their consumers expect specific non-wrapped formats.
 *
 * @param <T> type of the payload in the {@code data} field
 * @param success {@code true} for 2xx responses, {@code false} for error responses
 * @param data successful payload; {@code null} for error responses
 * @param error RFC 9457 problem details; {@code null} for successful responses
 * @param meta correlation and timestamp metadata attached to every response
 */
@Serdeable
@Schema(
    name = "ApiResponse",
    description = "Standard JSON response envelope for REST API responses.")
public record ApiResponse<T>(
    @Schema(description = "True only for 2xx responses.") boolean success,
    @Schema(description = "Successful payload. Null for error responses.") T data,
    @Schema(description = "Problem details payload. Null for successful responses.")
        ApiProblemDetails error,
    @Schema(description = "Response metadata.") ResponseMeta meta) {

  /**
   * Creates a successful response envelope with the given payload.
   *
   * @param <T> payload type
   * @param data payload; may be {@code null} for void responses
   * @param meta response metadata
   * @return envelope with {@code success=true} and {@code error=null}
   */
  public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
    return new ApiResponse<>(true, data, null, meta);
  }

  /**
   * Creates a failure response envelope with the given problem details.
   *
   * @param <T> payload type, typically {@link Void}
   * @param error RFC 9457 problem details describing the failure
   * @param meta response metadata
   * @return envelope with {@code success=false} and {@code data=null}
   */
  public static <T> ApiResponse<T> failure(ApiProblemDetails error, ResponseMeta meta) {
    return new ApiResponse<>(false, null, error, meta);
  }
}
