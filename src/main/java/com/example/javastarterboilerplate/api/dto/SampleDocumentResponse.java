package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.UUID;

/**
 * Response representation of a sample document resource.
 *
 * @param id unique document identifier
 * @param name display name
 * @param storageKey object store key pointing to the document content
 * @param createdAt creation timestamp in UTC
 */
@Serdeable
public record SampleDocumentResponse(UUID id, String name, String storageKey, Instant createdAt) {}
