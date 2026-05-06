package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

/**
 * Descriptor for an object upload operation.
 *
 * @param key storage key to upload under
 * @param contentType MIME type of the content, e.g. {@code "application/pdf"}
 * @param content raw bytes to store; must not be {@code null}
 * @param metadata arbitrary string metadata to attach to the stored object
 */
public record StoredObjectUpload(
    String key, String contentType, byte[] content, Map<String, String> metadata) {}
