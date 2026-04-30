package com.example.javastarterboilerplate.domain.document;

public interface PdfDocumentService {

  PdfDocumentDescriptor describe();

  PdfDocumentMetadata inspect(byte[] documentBytes);
}
