package com.example.javastarterboilerplate.domain.document;

/**
 * Structural metadata extracted from a PDF document.
 *
 * @param pageCount total number of pages
 * @param encrypted {@code true} if the document is password-protected or encrypted
 * @param version PDF specification version, e.g. {@code 1.7f} for PDF 1.7
 */
public record PdfDocumentMetadata(int pageCount, boolean encrypted, float version) {}
