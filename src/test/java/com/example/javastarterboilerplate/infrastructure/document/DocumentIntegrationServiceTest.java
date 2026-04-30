package com.example.javastarterboilerplate.infrastructure.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.javastarterboilerplate.domain.document.PdfDocumentMetadata;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

class DocumentIntegrationServiceTest {

  @Test
  void inspectsPdfDocumentsAndHandlesEnabledFlag() throws IOException {
    PdfBoxProperties pdfBoxProperties = new PdfBoxProperties();
    pdfBoxProperties.setEnabled(true);

    PdfBoxDocumentService service = new PdfBoxDocumentService(pdfBoxProperties);
    PdfDocumentMetadata metadata = service.inspect(samplePdf());

    assertThat(service.describe().detail()).contains("ready");
    assertThat(metadata.pageCount()).isEqualTo(1);
    assertThat(metadata.encrypted()).isFalse();
  }

  @Test
  void reportsDisabledPdfBoxIntegration() {
    PdfBoxProperties pdfBoxProperties = new PdfBoxProperties();
    pdfBoxProperties.setEnabled(false);

    PdfBoxDocumentService service = new PdfBoxDocumentService(pdfBoxProperties);

    assertThat(service.describe().detail()).contains("disabled");
  }

  @Test
  void failsFastForInvalidPdfContent() {
    PdfBoxDocumentService service = new PdfBoxDocumentService(new PdfBoxProperties());

    assertThatThrownBy(() -> service.inspect("not-a-pdf".getBytes()))
        .isInstanceOf(java.io.UncheckedIOException.class)
        .hasMessageContaining("Failed to inspect PDF document");
  }

  @Test
  void preparesDssDocumentsAndHandlesEnabledFlag() {
    DssProperties enabledProperties = new DssProperties();
    enabledProperties.setEnabled(true);

    DssDigitalSignatureService enabledService = new DssDigitalSignatureService(enabledProperties);
    var preparationResult = enabledService.prepare("pdf".getBytes(), "document.pdf");

    assertThat(enabledService.describe().detail()).contains("wired");
    assertThat(preparationResult.provider()).isEqualTo("dss");
    assertThat(preparationResult.fileName()).isEqualTo("document.pdf");

    DssProperties disabledProperties = new DssProperties();
    disabledProperties.setEnabled(false);
    DssDigitalSignatureService disabledService = new DssDigitalSignatureService(disabledProperties);

    assertThat(disabledService.describe().detail()).contains("disabled");
  }

  private byte[] samplePdf() throws IOException {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      document.addPage(new PDPage());
      document.save(outputStream);
      return outputStream.toByteArray();
    }
  }
}
