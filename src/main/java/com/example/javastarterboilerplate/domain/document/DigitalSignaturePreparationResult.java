package com.example.javastarterboilerplate.domain.document;

public record DigitalSignaturePreparationResult(
    String provider, String fileName, long byteSize, String mimeType) {}
