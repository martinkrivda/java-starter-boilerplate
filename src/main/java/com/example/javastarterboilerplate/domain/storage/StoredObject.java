package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

public record StoredObject(String key, String contentType, byte[] content, Map<String, String> metadata) {
}
