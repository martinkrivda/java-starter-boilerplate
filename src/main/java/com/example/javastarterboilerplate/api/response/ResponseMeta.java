package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Serdeable
@Schema(name = "ResponseMeta", description = "Metadata attached to every JSON envelope response.")
public record ResponseMeta(
    @Schema(
            description = "Request correlation identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        String requestId,
    @Schema(
            description = "Response timestamp in RFC 3339 format.",
            type = "string",
            format = "date-time")
        Instant timestamp) {}
