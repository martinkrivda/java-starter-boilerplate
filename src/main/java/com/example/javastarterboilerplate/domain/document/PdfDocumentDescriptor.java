package com.example.javastarterboilerplate.domain.document;

/**
 * Identifies the active PDF processing provider and its readiness state.
 *
 * @param provider short provider identifier, e.g. {@code "pdfbox"}
 * @param detail human-readable status detail returned by the adapter
 */
public record PdfDocumentDescriptor(String provider, String detail) {}
