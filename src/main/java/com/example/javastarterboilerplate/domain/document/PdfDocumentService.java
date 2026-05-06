package com.example.javastarterboilerplate.domain.document;

/**
 * Domain contract for PDF document operations.
 *
 * <p>Implementations live in the {@code infrastructure} layer. The current adapter uses Apache
 * PDFBox for document inspection. Future sealing and stamping operations will extend this contract
 * as the service scope grows.
 */
public interface PdfDocumentService {

  /**
   * Returns a human-readable descriptor identifying the active PDF provider.
   *
   * @return provider identity and readiness detail; never {@code null}
   */
  PdfDocumentDescriptor describe();

  /**
   * Extracts basic structural metadata from a PDF document without modifying it.
   *
   * @param documentBytes raw PDF bytes; must not be {@code null}
   * @return metadata including page count, encryption status and PDF version
   * @throws java.io.UncheckedIOException if the bytes do not represent a valid PDF
   */
  PdfDocumentMetadata inspect(byte[] documentBytes);
}
