package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

/**
 * An object retrieved from the object store.
 *
 * @param key storage key that identifies this object
 * @param contentType MIME type declared when the object was stored
 * @param content raw object bytes
 * @param metadata arbitrary string metadata attached to the object
 */
public record StoredObject(
    String key, String contentType, byte[] content, Map<String, String> metadata) {}
