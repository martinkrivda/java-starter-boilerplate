package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
@Schema(
    name = "FieldError",
    description = "Field-level validation error using JSON Pointer locations where available.")
public record ApiFieldError(
    @Schema(description = "JSON Pointer to the invalid input field.", example = "/customer/email")
        String pointer,
    @Schema(
            description = "Optional field name alias for clients that do not parse JSON Pointer.",
            example = "email")
        String field,
    @Schema(description = "Stable machine-readable field error code.", example = "REQUIRED")
        String code,
    @Schema(description = "Human-readable validation message.", example = "Field is required.")
        String message) {}
