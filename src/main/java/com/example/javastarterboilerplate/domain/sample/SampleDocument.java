package com.example.javastarterboilerplate.domain.sample;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain model representing a sample document record.
 *
 * <p>Used as a boilerplate example entity to demonstrate the persistence adapter pattern. Not
 * related to any real document processing workflow.
 *
 * @param id unique document identifier assigned at creation time
 * @param name display name of the document; max 128 characters
 * @param storageKey key used to locate the document in the object store; max 256 characters
 * @param createdAt creation timestamp in UTC
 */
public record SampleDocument(UUID id, String name, String storageKey, Instant createdAt) {}
