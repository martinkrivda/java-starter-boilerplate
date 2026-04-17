package com.example.javastarterboilerplate.infrastructure.document;

import com.example.javastarterboilerplate.domain.document.PdfDocumentDescriptor;
import com.example.javastarterboilerplate.domain.document.PdfDocumentMetadata;
import com.example.javastarterboilerplate.domain.document.PdfDocumentService;
import jakarta.inject.Singleton;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.io.UncheckedIOException;

@Singleton
public class PdfBoxDocumentService implements PdfDocumentService {

    private final PdfBoxProperties pdfBoxProperties;

    public PdfBoxDocumentService(PdfBoxProperties pdfBoxProperties) {
        this.pdfBoxProperties = pdfBoxProperties;
    }

    @Override
    public PdfDocumentDescriptor describe() {
        return new PdfDocumentDescriptor("pdfbox",
                pdfBoxProperties.isEnabled()
                        ? "PDFBox integration is ready for document inspection and future sealing support"
                        : "PDFBox integration is disabled");
    }

    @Override
    public PdfDocumentMetadata inspect(byte[] documentBytes) {
        try (PDDocument document = Loader.loadPDF(documentBytes)) {
            return new PdfDocumentMetadata(document.getNumberOfPages(), document.isEncrypted(), document.getVersion());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to inspect PDF document", exception);
        }
    }
}
