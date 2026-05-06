package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a new sample document record.
 *
 * @param name display name of the document; must not be blank; max 128 characters
 * @param storageKey key in the object store pointing to the document content; must not be blank
 */
@Serdeable
public record CreateSampleDocumentRequest(@NotBlank String name, @NotBlank String storageKey) {}
