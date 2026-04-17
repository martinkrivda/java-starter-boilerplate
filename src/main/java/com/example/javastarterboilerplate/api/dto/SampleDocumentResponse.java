package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record SampleDocumentResponse(UUID id, String name, String storageKey, Instant createdAt) {
}
