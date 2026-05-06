package com.example.javastarterboilerplate.domain.document;

/**
 * Result of a digital signature preparation step.
 *
 * <p>Carries the information the caller needs to proceed with signing without re-parsing the
 * document.
 *
 * @param provider short provider identifier, e.g. {@code "dss"}
 * @param fileName original document file name including extension
 * @param byteSize raw document size in bytes
 * @param mimeType MIME type of the document, e.g. {@code "application/pdf"}
 */
public record DigitalSignaturePreparationResult(
    String provider, String fileName, long byteSize, String mimeType) {}
