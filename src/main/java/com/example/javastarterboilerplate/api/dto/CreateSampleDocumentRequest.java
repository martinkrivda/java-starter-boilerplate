package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record CreateSampleDocumentRequest(@NotBlank String name, @NotBlank String storageKey) {
}
