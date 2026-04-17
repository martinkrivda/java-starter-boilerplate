package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Serdeable
@Schema(name = "ProblemDetails", description = "RFC 9457 compatible problem details with company extensions.")
public record ApiProblemDetails(
        @Schema(description = "Absolute URI identifying the problem type.", example = "https://api.example.com/problems/validation-error") String type,
        @Schema(description = "Short problem title.", example = "Validation error") String title,
        @Schema(description = "HTTP status code mirrored from the response status.", example = "422") int status,
        @Schema(description = "Safe human-readable detail message.", example = "Request is not valid.") String detail,
        @Schema(description = "Request path without query string.", example = "/rest/v1/sample-documents") String instance,
        @Schema(description = "Stable internal error code.", example = "E1003") String code,
        @Schema(description = "Request correlation identifier.", example = "550e8400-e29b-41d4-a716-446655440000") String requestId,
        @Schema(description = "Field-level validation errors. Present only for validation problems.") List<ApiFieldError> errors) {
}
