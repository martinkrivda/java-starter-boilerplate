package com.example.javastarterboilerplate.infrastructure.document;

import com.example.javastarterboilerplate.domain.document.PdfDocumentDescriptor;
import com.example.javastarterboilerplate.domain.document.PdfDocumentMetadata;
import com.example.javastarterboilerplate.domain.document.PdfDocumentService;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Infrastructure adapter implementing {@link
 * com.example.javastarterboilerplate.domain.document.PdfDocumentService} using Apache PDFBox.
 *
 * <p>Currently handles document inspection only. Future sealing and stamping operations will extend
 * this adapter as the service scope grows. The adapter is always registered regardless of the
 * {@code pdfbox.enabled} flag; the flag controls only the detail message in {@link #describe()}.
 */
@Singleton
public class PdfBoxDocumentService implements PdfDocumentService {

  private final PdfBoxProperties pdfBoxProperties;

  public PdfBoxDocumentService(PdfBoxProperties pdfBoxProperties) {
    this.pdfBoxProperties = pdfBoxProperties;
  }

  @Override
  public PdfDocumentDescriptor describe() {
    return new PdfDocumentDescriptor(
        "pdfbox",
        pdfBoxProperties.isEnabled()
            ? "PDFBox integration is ready for document inspection and future sealing support"
            : "PDFBox integration is disabled");
  }

  /**
   * Parses the given PDF bytes and extracts page count, encryption status and version.
   *
   * @param documentBytes raw PDF bytes; must not be {@code null}
   * @return extracted metadata
   * @throws java.io.UncheckedIOException if {@code documentBytes} is not a valid PDF
   */
  @Override
  public PdfDocumentMetadata inspect(byte[] documentBytes) {
    try (PDDocument document = Loader.loadPDF(documentBytes)) {
      return new PdfDocumentMetadata(
          document.getNumberOfPages(), document.isEncrypted(), document.getVersion());
    } catch (IOException exception) {
      throw new UncheckedIOException("Failed to inspect PDF document", exception);
    }
  }
}
