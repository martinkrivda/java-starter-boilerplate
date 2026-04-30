package com.example.javastarterboilerplate.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.api.dto.ApplicationComponentStatusResponse;
import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.api.dto.CreateSampleDocumentRequest;
import com.example.javastarterboilerplate.api.dto.HealthCheckResponse;
import com.example.javastarterboilerplate.api.dto.HealthFullResponse;
import com.example.javastarterboilerplate.api.dto.HealthLivenessResponse;
import com.example.javastarterboilerplate.api.dto.HealthReadinessResponse;
import com.example.javastarterboilerplate.api.dto.SampleDocumentResponse;
import com.example.javastarterboilerplate.api.response.ApiFieldError;
import com.example.javastarterboilerplate.api.response.ApiProblemDetails;
import com.example.javastarterboilerplate.api.response.ApiResponse;
import com.example.javastarterboilerplate.api.response.ResponseMeta;
import com.example.javastarterboilerplate.domain.document.DigitalSignatureDescriptor;
import com.example.javastarterboilerplate.domain.document.DigitalSignaturePreparationResult;
import com.example.javastarterboilerplate.domain.document.PdfDocumentDescriptor;
import com.example.javastarterboilerplate.domain.document.PdfDocumentMetadata;
import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.storage.StoredObject;
import com.example.javastarterboilerplate.domain.storage.StoredObjectUpload;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecordContractTest {

  @Test
  void exposesRecordState() {
    ApplicationComponentStatusResponse component =
        new ApplicationComponentStatusResponse("storage", true, "ready");
    ApplicationInfoResponse info =
        new ApplicationInfoResponse(
            "starter", "1.0.0", "desc", "postgresql", List.of("dev"), List.of(component));
    CreateSampleDocumentRequest request = new CreateSampleDocumentRequest("name", "storage-key");
    HealthCheckResponse healthCheck =
        new HealthCheckResponse("database", "UP", 1L, "ready", Map.of("enabled", true));
    HealthFullResponse health = new HealthFullResponse("UP", "1.0.0", 1L, List.of(healthCheck));
    HealthReadinessResponse readiness = new HealthReadinessResponse("ready", List.of(healthCheck));
    HealthLivenessResponse liveness = new HealthLivenessResponse("UP");
    UUID id = UUID.randomUUID();
    SampleDocumentResponse response =
        new SampleDocumentResponse(
            id, "name", "storage-key", Instant.parse("2026-04-13T12:00:00Z"));
    ApiFieldError fieldError =
        new ApiFieldError("/customer/email", "email", "INVALID_FORMAT", "bad format");
    ApiProblemDetails problemDetails =
        new ApiProblemDetails(
            "https://api.example.com/problems/validation-error",
            "Validation error",
            422,
            "Request is not valid.",
            "/rest/v1/orders",
            "E1003",
            "550e8400-e29b-41d4-a716-446655440000",
            List.of(fieldError));
    ResponseMeta meta =
        new ResponseMeta(
            "550e8400-e29b-41d4-a716-446655440000", Instant.parse("2026-04-13T12:00:00Z"));
    ApiResponse<HealthFullResponse> successEnvelope = ApiResponse.success(health, meta);
    ApiResponse<Void> errorEnvelope = ApiResponse.failure(problemDetails, meta);
    DigitalSignatureDescriptor signatureDescriptor = new DigitalSignatureDescriptor("dss", "ready");
    DigitalSignaturePreparationResult signaturePreparationResult =
        new DigitalSignaturePreparationResult("dss", "doc.pdf", 10, "application/pdf");
    PdfDocumentDescriptor pdfDescriptor = new PdfDocumentDescriptor("pdfbox", "ready");
    PdfDocumentMetadata pdfMetadata = new PdfDocumentMetadata(2, false, 1.7f);
    SampleDocument sampleDocument =
        new SampleDocument(id, "name", "storage-key", Instant.parse("2026-04-13T12:00:00Z"));
    StoredObject storedObject =
        new StoredObject(
            "object-key", "application/pdf", new byte[] {1, 2}, Map.of("source", "test"));
    StoredObjectUpload upload =
        new StoredObjectUpload(
            "object-key", "application/pdf", new byte[] {1, 2}, Map.of("source", "test"));

    assertThat(component.component()).isEqualTo("storage");
    assertThat(info.activeEnvironments()).containsExactly("dev");
    assertThat(request.storageKey()).isEqualTo("storage-key");
    assertThat(health.version()).isEqualTo("1.0.0");
    assertThat(readiness.status()).isEqualTo("ready");
    assertThat(liveness.status()).isEqualTo("UP");
    assertThat(response.id()).isEqualTo(id);
    assertThat(fieldError.pointer()).isEqualTo("/customer/email");
    assertThat(problemDetails.errors()).hasSize(1);
    assertThat(successEnvelope.success()).isTrue();
    assertThat(successEnvelope.data()).isEqualTo(health);
    assertThat(errorEnvelope.error()).isEqualTo(problemDetails);
    assertThat(meta.requestId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    assertThat(signatureDescriptor.provider()).isEqualTo("dss");
    assertThat(signaturePreparationResult.fileName()).isEqualTo("doc.pdf");
    assertThat(pdfDescriptor.provider()).isEqualTo("pdfbox");
    assertThat(pdfMetadata.pageCount()).isEqualTo(2);
    assertThat(sampleDocument.id()).isEqualTo(id);
    assertThat(healthCheck.responseTimeMs()).isEqualTo(1L);
    assertThat(storedObject.metadata()).containsEntry("source", "test");
    assertThat(upload.content()).hasSize(2);
  }
}
