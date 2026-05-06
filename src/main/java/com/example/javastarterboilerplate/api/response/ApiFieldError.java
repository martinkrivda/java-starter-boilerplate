package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single field-level validation error using RFC 6901 JSON Pointer locations.
 *
 * <p>Included in {@link ApiProblemDetails#errors()} when a request fails validation. The {@code
 * pointer} field follows JSON Pointer syntax (RFC 6901); the {@code field} alias is provided for
 * clients that do not parse JSON Pointers.
 *
 * @param pointer JSON Pointer path to the invalid field, e.g. {@code "/customer/email"}
 * @param field last path segment of the pointer as a convenience alias, e.g. {@code "email"}
 * @param code stable machine-readable error code for the constraint violation
 * @param message human-readable description of the constraint that was violated
 */
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
