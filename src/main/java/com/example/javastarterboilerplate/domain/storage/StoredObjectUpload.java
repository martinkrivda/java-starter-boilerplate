package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

public record StoredObjectUpload(
    String key, String contentType, byte[] content, Map<String, String> metadata) {}
