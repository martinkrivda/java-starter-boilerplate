package com.example.javastarterboilerplate.domain.sample;

import java.time.Instant;
import java.util.UUID;

public record SampleDocument(UUID id, String name, String storageKey, Instant createdAt) {}
