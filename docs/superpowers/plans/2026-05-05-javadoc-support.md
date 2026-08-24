# Javadoc Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Configure Gradle Javadoc generation, add Makefile targets, and add meaningful Javadoc comments to all public production classes.

**Architecture:** The project uses Gradle (Kotlin DSL) with the Java plugin transitively included through `micronaut.application`. The standard `javadoc` Gradle task is already present but unconfigured. We configure it, wire Makefile targets, add Javadoc to ~40 production source files grouped by package, then update the README.

**Tech Stack:** Gradle 8.x (Kotlin DSL), Java 25, Micronaut 4, Google Java Format (Spotless)

---

## File Map

| File | Action |
|------|--------|
| `build.gradle.kts` | Add `javadoc` task configuration block |
| `Makefile` | Add `docs`, `javadoc`, `clean-docs` targets; update `help` and `.PHONY` |
| `README.md` | Add "Javadoc" section under Local Run |
| `src/main/java/.../domain/document/DigitalSignatureService.java` | Add Javadoc |
| `src/main/java/.../domain/document/PdfDocumentService.java` | Add Javadoc |
| `src/main/java/.../domain/document/DigitalSignatureDescriptor.java` | Add Javadoc |
| `src/main/java/.../domain/document/DigitalSignaturePreparationResult.java` | Add Javadoc |
| `src/main/java/.../domain/document/PdfDocumentDescriptor.java` | Add Javadoc |
| `src/main/java/.../domain/document/PdfDocumentMetadata.java` | Add Javadoc |
| `src/main/java/.../domain/sample/SampleDocument.java` | Add Javadoc |
| `src/main/java/.../domain/sample/SampleDocumentRepository.java` | Add Javadoc |
| `src/main/java/.../domain/storage/ObjectStorage.java` | Add Javadoc |
| `src/main/java/.../domain/storage/StoredObject.java` | Add Javadoc |
| `src/main/java/.../domain/storage/StoredObjectUpload.java` | Add Javadoc |
| `src/main/java/.../api/response/ApiResponse.java` | Add Javadoc |
| `src/main/java/.../api/response/ApiResponseFactory.java` | Add Javadoc |
| `src/main/java/.../api/response/ApiProblemDetails.java` | Add Javadoc |
| `src/main/java/.../api/response/ApiFieldError.java` | Add Javadoc |
| `src/main/java/.../api/response/ResponseMeta.java` | Add Javadoc |
| `src/main/java/.../api/controller/GlobalErrorHandler.java` | Add Javadoc |
| `src/main/java/.../api/controller/SampleDocumentController.java` | Add Javadoc |
| `src/main/java/.../api/controller/HealthController.java` | Add Javadoc |
| `src/main/java/.../api/controller/InfoController.java` | Add Javadoc |
| `src/main/java/.../api/controller/RootController.java` | Add Javadoc |
| `src/main/java/.../api/controller/MetricsController.java` | Add Javadoc |
| `src/main/java/.../api/controller/DocumentationController.java` | Add Javadoc |
| `src/main/java/.../api/dto/*.java` (7 files) | Add Javadoc |
| `src/main/java/.../application/ApplicationInfoService.java` | Add Javadoc |
| `src/main/java/.../application/ApplicationShutdownState.java` | Add Javadoc |
| `src/main/java/.../application/documentation/OpenApiDocumentationService.java` | Add Javadoc |
| `src/main/java/.../application/sample/SampleDocumentService.java` | Add Javadoc |
| `src/main/java/.../config/ApiResponseProperties.java` | Add Javadoc |
| `src/main/java/.../config/ApplicationInfoProperties.java` | Add Javadoc |
| `src/main/java/.../config/ClockFactory.java` | Add Javadoc |
| `src/main/java/.../config/OpenApiDocumentationProperties.java` | Add Javadoc |
| `src/main/java/.../infrastructure/document/PdfBoxDocumentService.java` | Add Javadoc |
| `src/main/java/.../infrastructure/document/DssDigitalSignatureService.java` | Add Javadoc |
| `src/main/java/.../infrastructure/persistence/SampleDocumentEntity.java` | Add Javadoc |
| `src/main/java/.../infrastructure/persistence/InMemorySampleDocumentRepository.java` | Add Javadoc |
| `src/main/java/.../infrastructure/persistence/SampleDocumentPersistenceAdapter.java` | Add Javadoc |
| `src/main/java/.../infrastructure/persistence/PersistenceProperties.java` | Add Javadoc |
| `src/main/java/.../infrastructure/storage/S3ClientFactory.java` | Add Javadoc |
| `src/main/java/.../infrastructure/storage/S3ObjectStorageAdapter.java` | Add Javadoc |
| `src/main/java/.../infrastructure/storage/S3StorageProperties.java` | Add Javadoc |
| `src/main/java/.../observability/RequestIdFilter.java` | Add Javadoc |
| `src/main/java/.../observability/GracefulShutdownListener.java` | Add Javadoc |
| `src/main/java/.../Application.java` | Add Javadoc |

---

## Task 1: Configure Gradle `javadoc` Task and Makefile Targets

**Files:**
- Modify: `build.gradle.kts`
- Modify: `Makefile`

- [ ] **Step 1: Add the `javadoc` task configuration to `build.gradle.kts`**

Add the following import at the top of `build.gradle.kts` (after the existing imports):

```kotlin
import org.gradle.external.javadoc.StandardJavadocDocletOptions
```

Then add this task block **before** the `tasks.check` block at the bottom of `build.gradle.kts`:

```kotlin
tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        charSet("UTF-8")
        addBooleanOption("Xdoclint:none", true)
        windowTitle = "${rootProject.name} $projectVersion"
        docTitle = "${rootProject.name} $projectVersion"
    }
}
```

`-Xdoclint:none` prevents the task from failing on pre-existing code without full Javadoc. Output lands in `build/docs/javadoc/`.

- [ ] **Step 2: Verify `./gradlew javadoc` runs cleanly (baseline)**

```bash
./gradlew javadoc
```

Expected: `BUILD SUCCESSFUL` and `build/docs/javadoc/index.html` exists. If it fails, fix compilation errors before continuing.

- [ ] **Step 3: Add Makefile targets**

In the `.PHONY` declaration at line 9 of `Makefile`, add `docs javadoc clean-docs`:

```makefile
.PHONY: help doctor run run-postgresql run-sqlserver test check format format-check dependency-updates jar cli-help cli-version cli-env docker-build compose-up compose-down compose-logs docker-test docker-check docker-format docker-jar docker-clean clean clean-deep reset-workspace docs javadoc clean-docs
```

Add the three new targets after the `clean-deep` target:

```makefile
docs:
	$(GRADLE) javadoc

javadoc: docs

clean-docs:
	rm -rf build/docs
```

Update the `help` target to document the new targets. Add these three lines inside the `help` target after the `clean-deep` line:

```makefile
	@printf "  make docs            Generate Javadoc into build/docs/javadoc\n"
	@printf "  make javadoc         Alias for make docs\n"
	@printf "  make clean-docs      Remove generated Javadoc output\n"
```

- [ ] **Step 4: Verify Makefile targets work**

```bash
make docs
```

Expected: `BUILD SUCCESSFUL`. Confirm `build/docs/javadoc/index.html` exists.

```bash
make javadoc
```

Expected: same as above (alias).

```bash
make clean-docs
ls build/docs 2>/dev/null || echo "removed"
```

Expected: `removed` or directory listing without `javadoc`.

- [ ] **Step 5: Run Spotless on the changed files**

```bash
./gradlew spotlessApply
```

Expected: `BUILD SUCCESSFUL`. The Kotlin DSL formatter will validate `build.gradle.kts`. The `misc` formatter will check the `Makefile`.

- [ ] **Step 6: Commit**

```bash
git add build.gradle.kts Makefile
git commit -m "chore(build): configure Gradle javadoc task and add Makefile targets"
```

---

## Task 2: Add Javadoc to Domain Layer

All source file paths start with:
`src/main/java/com/example/javastarterboilerplate/`

**Files:**
- Modify: `domain/document/DigitalSignatureService.java`
- Modify: `domain/document/PdfDocumentService.java`
- Modify: `domain/document/DigitalSignatureDescriptor.java`
- Modify: `domain/document/DigitalSignaturePreparationResult.java`
- Modify: `domain/document/PdfDocumentDescriptor.java`
- Modify: `domain/document/PdfDocumentMetadata.java`
- Modify: `domain/sample/SampleDocument.java`
- Modify: `domain/sample/SampleDocumentRepository.java`
- Modify: `domain/storage/ObjectStorage.java`
- Modify: `domain/storage/StoredObject.java`
- Modify: `domain/storage/StoredObjectUpload.java`

- [ ] **Step 1: Add Javadoc to `domain/document/DigitalSignatureService.java`**

Replace the entire file content with:

```java
package com.example.javastarterboilerplate.domain.document;

/**
 * Domain contract for digital signature operations.
 *
 * <p>Implementations live in the {@code infrastructure} layer. The current adapter wires DSS
 * (EU Digital Signature Service) for future PAdES/CAdES workflows. The domain layer remains
 * free of any DSS or framework dependencies.
 */
public interface DigitalSignatureService {

  /**
   * Returns a human-readable descriptor identifying the active signature provider.
   *
   * @return provider identity and readiness detail; never {@code null}
   */
  DigitalSignatureDescriptor describe();

  /**
   * Prepares a document for signing without performing the actual signing operation.
   *
   * <p>This is a placeholder method for the future signing workflow. The current implementation
   * inspects the document and returns metadata required by the caller to initiate signing.
   *
   * @param documentBytes raw PDF document bytes; must not be {@code null} or empty
   * @param fileName original file name including extension, used as the DSS document name
   * @return preparation result containing provider, file name, byte size and MIME type
   */
  DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName);
}
```

- [ ] **Step 2: Add Javadoc to `domain/document/PdfDocumentService.java`**

Replace the entire file content with:

```java
package com.example.javastarterboilerplate.domain.document;

/**
 * Domain contract for PDF document operations.
 *
 * <p>Implementations live in the {@code infrastructure} layer. The current adapter uses Apache
 * PDFBox for document inspection. Future sealing and stamping operations will extend this
 * contract as the service scope grows.
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
```

- [ ] **Step 3: Add Javadoc to domain document value records**

Replace `domain/document/DigitalSignatureDescriptor.java`:

```java
package com.example.javastarterboilerplate.domain.document;

/**
 * Identifies the active digital signature provider and its readiness state.
 *
 * @param provider short provider identifier, e.g. {@code "dss"}
 * @param detail human-readable status detail returned by the adapter
 */
public record DigitalSignatureDescriptor(String provider, String detail) {}
```

Replace `domain/document/DigitalSignaturePreparationResult.java`:

```java
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
```

Replace `domain/document/PdfDocumentDescriptor.java`:

```java
package com.example.javastarterboilerplate.domain.document;

/**
 * Identifies the active PDF processing provider and its readiness state.
 *
 * @param provider short provider identifier, e.g. {@code "pdfbox"}
 * @param detail human-readable status detail returned by the adapter
 */
public record PdfDocumentDescriptor(String provider, String detail) {}
```

Replace `domain/document/PdfDocumentMetadata.java`:

```java
package com.example.javastarterboilerplate.domain.document;

/**
 * Structural metadata extracted from a PDF document.
 *
 * @param pageCount total number of pages
 * @param encrypted {@code true} if the document is password-protected or encrypted
 * @param version PDF specification version, e.g. {@code 1.7f} for PDF 1.7
 */
public record PdfDocumentMetadata(int pageCount, boolean encrypted, float version) {}
```

- [ ] **Step 4: Add Javadoc to domain sample types**

Replace `domain/sample/SampleDocument.java`:

```java
package com.example.javastarterboilerplate.domain.sample;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain model representing a sample document record.
 *
 * <p>Used as a boilerplate example entity to demonstrate the persistence adapter pattern.
 * Not related to any real document processing workflow.
 *
 * @param id unique document identifier assigned at creation time
 * @param name display name of the document; max 128 characters
 * @param storageKey key used to locate the document in the object store; max 256 characters
 * @param createdAt creation timestamp in UTC
 */
public record SampleDocument(UUID id, String name, String storageKey, Instant createdAt) {}
```

Replace `domain/sample/SampleDocumentRepository.java`:

```java
package com.example.javastarterboilerplate.domain.sample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for {@link SampleDocument} persistence.
 *
 * <p>Implementations are in the {@code infrastructure} layer. Two adapters exist:
 * {@code InMemorySampleDocumentRepository} (used when {@code persistence.enabled=false}) and
 * {@code SampleDocumentPersistenceAdapter} backed by JPA (used when
 * {@code persistence.enabled=true}).
 */
public interface SampleDocumentRepository {

  /**
   * Returns all sample documents, ordered by creation time descending.
   *
   * @return unmodifiable list; never {@code null}
   */
  List<SampleDocument> findAll();

  /**
   * Finds a single sample document by its identifier.
   *
   * @param id document identifier; must not be {@code null}
   * @return the document, or {@link java.util.Optional#empty()} if not found
   */
  Optional<SampleDocument> findById(UUID id);

  /**
   * Persists a new or updated document.
   *
   * @param sampleDocument document to save; must not be {@code null}
   * @return the saved document, which may be the same instance or a new object
   */
  SampleDocument save(SampleDocument sampleDocument);
}
```

- [ ] **Step 5: Add Javadoc to domain storage types**

Replace `domain/storage/ObjectStorage.java`:

```java
package com.example.javastarterboilerplate.domain.storage;

/**
 * Domain contract for binary object storage (key-value store).
 *
 * <p>The current infrastructure adapter, {@code S3ObjectStorageAdapter}, targets AWS S3 or
 * MinIO. The contract is intentionally minimal: download, upload, delete. Storage keys are
 * opaque strings — callers are responsible for key naming conventions.
 */
public interface ObjectStorage {

  /**
   * Downloads a stored object by its key.
   *
   * @param key storage key; must not be {@code null} or blank
   * @return the stored object including content and metadata
   * @throws software.amazon.awssdk.services.s3.model.NoSuchKeyException if the key does not exist
   */
  StoredObject download(String key);

  /**
   * Uploads a new object or overwrites an existing one at the given key.
   *
   * @param upload upload descriptor including key, content type, bytes and metadata
   * @return the resulting stored object reflecting what was persisted
   */
  StoredObject upload(StoredObjectUpload upload);

  /**
   * Deletes the object at the given key.
   *
   * <p>This operation is idempotent: deleting a non-existent key does not throw.
   *
   * @param key storage key; must not be {@code null} or blank
   */
  void delete(String key);
}
```

Replace `domain/storage/StoredObject.java`:

```java
package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

/**
 * An object retrieved from the object store.
 *
 * @param key storage key that identifies this object
 * @param contentType MIME type declared when the object was stored
 * @param content raw object bytes
 * @param metadata arbitrary string metadata attached to the object
 */
public record StoredObject(
    String key, String contentType, byte[] content, Map<String, String> metadata) {}
```

Replace `domain/storage/StoredObjectUpload.java`:

```java
package com.example.javastarterboilerplate.domain.storage;

import java.util.Map;

/**
 * Descriptor for an object upload operation.
 *
 * @param key storage key to upload under
 * @param contentType MIME type of the content, e.g. {@code "application/pdf"}
 * @param content raw bytes to store; must not be {@code null}
 * @param metadata arbitrary string metadata to attach to the stored object
 */
public record StoredObjectUpload(
    String key, String contentType, byte[] content, Map<String, String> metadata) {}
```

- [ ] **Step 6: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`. Confirm `build/docs/javadoc/index.html` reflects domain classes.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/domain/
git commit -m "docs(domain): add Javadoc to domain layer interfaces and records"
```

---

## Task 3: Add Javadoc to API Response Classes

**Files:**
- Modify: `api/response/ApiResponse.java`
- Modify: `api/response/ApiResponseFactory.java`
- Modify: `api/response/ApiProblemDetails.java`
- Modify: `api/response/ApiFieldError.java`
- Modify: `api/response/ResponseMeta.java`

- [ ] **Step 1: Add Javadoc to `api/response/ApiResponse.java`**

Replace file:

```java
package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard JSON response envelope for all REST API endpoints.
 *
 * <p>Every JSON response — success or error — is wrapped in this envelope. Use the static
 * factory methods {@link #success} and {@link #failure} to construct instances. Callers
 * typically do not construct this record directly; use {@link ApiResponseFactory} instead.
 *
 * <p>Health, metrics and OpenAPI document endpoints are deliberately excluded from this
 * envelope because their consumers expect specific non-wrapped formats.
 *
 * @param <T> type of the payload in the {@code data} field
 * @param success {@code true} for 2xx responses, {@code false} for error responses
 * @param data successful payload; {@code null} for error responses
 * @param error RFC 9457 problem details; {@code null} for successful responses
 * @param meta correlation and timestamp metadata attached to every response
 */
@Serdeable
@Schema(
    name = "ApiResponse",
    description = "Standard JSON response envelope for REST API responses.")
public record ApiResponse<T>(
    @Schema(description = "True only for 2xx responses.") boolean success,
    @Schema(description = "Successful payload. Null for error responses.") T data,
    @Schema(description = "Problem details payload. Null for successful responses.")
        ApiProblemDetails error,
    @Schema(description = "Response metadata.") ResponseMeta meta) {

  /**
   * Creates a successful response envelope with the given payload.
   *
   * @param <T> payload type
   * @param data payload; may be {@code null} for void responses
   * @param meta response metadata
   * @return envelope with {@code success=true} and {@code error=null}
   */
  public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
    return new ApiResponse<>(true, data, null, meta);
  }

  /**
   * Creates a failure response envelope with the given problem details.
   *
   * @param <T> payload type (typically {@link Void})
   * @param error RFC 9457 problem details describing the failure
   * @param meta response metadata
   * @return envelope with {@code success=false} and {@code data=null}
   */
  public static <T> ApiResponse<T> failure(ApiProblemDetails error, ResponseMeta meta) {
    return new ApiResponse<>(false, null, error, meta);
  }
}
```

- [ ] **Step 2: Add Javadoc to `api/response/ApiResponseFactory.java`**

Replace file:

```java
package com.example.javastarterboilerplate.api.response;

import com.example.javastarterboilerplate.config.ApiResponseProperties;
import com.example.javastarterboilerplate.observability.RequestIdFilter;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Centralised factory for building {@link ApiResponse}-wrapped HTTP responses.
 *
 * <p>Controllers delegate all response construction to this factory to guarantee consistent
 * envelope structure, metadata population, and problem-type URI generation across every
 * endpoint.
 */
@Singleton
public class ApiResponseFactory {

  private final Clock clock;
  private final ApiResponseProperties properties;

  /**
   * @param clock UTC clock injected by {@code ClockFactory}; used for response timestamps
   * @param properties configuration for the problem base URI
   */
  public ApiResponseFactory(Clock clock, ApiResponseProperties properties) {
    this.clock = clock;
    this.properties = properties;
  }

  /**
   * Builds a successful {@link ApiResponse} without wrapping it in an HTTP response object.
   *
   * @param <T> payload type
   * @param request incoming request used to extract the correlation identifier
   * @param data payload to include
   * @return envelope with {@code success=true}
   */
  public <T> ApiResponse<T> success(HttpRequest<?> request, T data) {
    return ApiResponse.success(data, meta(request));
  }

  /**
   * Builds a 200 OK HTTP response wrapping the payload in an {@link ApiResponse} envelope.
   *
   * @param <T> payload type
   * @param request incoming request
   * @param data payload
   * @return 200 OK response
   */
  public <T> MutableHttpResponse<ApiResponse<T>> ok(HttpRequest<?> request, T data) {
    return HttpResponse.ok(success(request, data));
  }

  /**
   * Builds a 201 Created HTTP response with a {@code Location} header.
   *
   * @param <T> payload type
   * @param request incoming request
   * @param data created resource representation
   * @param location URI of the newly created resource
   * @return 201 Created response with {@code Location} set to {@code location}
   */
  public <T> MutableHttpResponse<ApiResponse<T>> created(
      HttpRequest<?> request, T data, URI location) {
    return HttpResponse.created(success(request, data))
        .headers(headers -> headers.location(location));
  }

  /**
   * Builds an error HTTP response conforming to RFC 9457 inside the {@link ApiResponse} envelope.
   *
   * <p>The response body sets {@code success=false} and populates the {@code error} field.
   * A {@code Cache-Control: no-store} header is added to prevent caching of error responses.
   *
   * @param request incoming request
   * @param status HTTP status for the error
   * @param typeSlug last path segment appended to the problem base URI to form the type URI
   * @param title short human-readable problem title
   * @param detail safe human-readable error detail (must not expose internal state for 5xx)
   * @param code stable internal error code, e.g. {@code "E1003"}
   * @param errors field-level validation errors; may be {@code null} or empty
   * @return HTTP response with the given status and error envelope body
   */
  public MutableHttpResponse<ApiResponse<Void>> error(
      HttpRequest<?> request,
      HttpStatus status,
      String typeSlug,
      String title,
      String detail,
      String code,
      List<ApiFieldError> errors) {
    ResponseMeta meta = meta(request);
    ApiProblemDetails problem =
        new ApiProblemDetails(
            problemType(typeSlug),
            title,
            status.getCode(),
            detail,
            request.getPath(),
            code,
            meta.requestId(),
            errors == null || errors.isEmpty() ? null : errors);
    return HttpResponse.<ApiResponse<Void>>status(status)
        .body(ApiResponse.<Void>failure(problem, meta))
        .contentType("application/json")
        .header(HttpHeaders.CACHE_CONTROL, "no-store");
  }

  private ResponseMeta meta(HttpRequest<?> request) {
    return new ResponseMeta(requestId(request), Instant.now(clock));
  }

  private String requestId(HttpRequest<?> request) {
    return request
        .getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, String.class)
        .orElse("not-set");
  }

  private String problemType(String typeSlug) {
    String baseUri = properties.getProblemBaseUri();
    if (baseUri.endsWith("/")) {
      return baseUri + typeSlug;
    }
    return baseUri + "/" + typeSlug;
  }
}
```

- [ ] **Step 3: Add Javadoc to `api/response/ApiProblemDetails.java`**

Replace file:

```java
package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * RFC 9457-compatible problem details embedded in the {@link ApiResponse} error envelope.
 *
 * <p>All error responses populate this record. The {@code type} URI identifies the error
 * category and is stable across releases, making it safe for clients to use programmatically.
 * The {@code code} field provides an alternative stable machine-readable identifier.
 *
 * @param type absolute URI identifying the problem type; stable across releases
 * @param title short, human-readable problem title matching the type
 * @param status HTTP status code mirrored from the response
 * @param detail safe human-readable description of the specific occurrence
 * @param instance request path without query string, pointing to the failing resource
 * @param code stable internal error code, e.g. {@code "E1003"}
 * @param requestId correlation identifier from {@code X-Request-Id}
 * @param errors field-level validation errors; present only for validation failures
 */
@Serdeable
@Schema(
    name = "ProblemDetails",
    description = "RFC 9457 compatible problem details with company extensions.")
public record ApiProblemDetails(
    @Schema(
            description = "Absolute URI identifying the problem type.",
            example = "https://api.example.com/problems/validation-error")
        String type,
    @Schema(description = "Short problem title.", example = "Validation error") String title,
    @Schema(description = "HTTP status code mirrored from the response status.", example = "422")
        int status,
    @Schema(description = "Safe human-readable detail message.", example = "Request is not valid.")
        String detail,
    @Schema(
            description = "Request path without query string.",
            example = "/rest/v1/sample-documents")
        String instance,
    @Schema(description = "Stable internal error code.", example = "E1003") String code,
    @Schema(
            description = "Request correlation identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        String requestId,
    @Schema(description = "Field-level validation errors. Present only for validation problems.")
        List<ApiFieldError> errors) {}
```

- [ ] **Step 4: Add Javadoc to `api/response/ApiFieldError.java`**

Replace file:

```java
package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single field-level validation error using RFC 6901 JSON Pointer locations.
 *
 * <p>Included in {@link ApiProblemDetails#errors()} when a request fails validation.
 * The {@code pointer} field follows JSON Pointer syntax (RFC 6901); the {@code field}
 * alias is provided for clients that do not parse JSON Pointers.
 *
 * @param pointer JSON Pointer path to the invalid field, e.g. {@code "/customer/email"}
 * @param field last path segment of the pointer as a convenience alias, e.g. {@code "email"}
 * @param code stable machine-readable error code for the constraint violation
 * @param message human-readable description of the constraint that was violated
 */
@Serdeable
@Schema(
    name = "FieldError",
    description = "Field-level validation error using JSON Pointer locations where available.")
public record ApiFieldError(
    @Schema(description = "JSON Pointer to the invalid input field.", example = "/customer/email")
        String pointer,
    @Schema(
            description = "Optional field name alias for clients that do not parse JSON Pointer.",
            example = "email")
        String field,
    @Schema(description = "Stable machine-readable field error code.", example = "REQUIRED")
        String code,
    @Schema(description = "Human-readable validation message.", example = "Field is required.")
        String message) {}
```

- [ ] **Step 5: Add Javadoc to `api/response/ResponseMeta.java`**

Replace file:

```java
package com.example.javastarterboilerplate.api.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Metadata attached to every {@link ApiResponse} envelope.
 *
 * <p>Allows clients to correlate responses with log entries and distributed traces using
 * {@code requestId}, and to detect clock skew using {@code timestamp}.
 *
 * @param requestId correlation identifier propagated from the {@code X-Request-Id} header,
 *     or a generated UUID if the header was absent
 * @param timestamp server-side UTC timestamp at which the response was produced
 */
@Serdeable
@Schema(name = "ResponseMeta", description = "Metadata attached to every JSON envelope response.")
public record ResponseMeta(
    @Schema(
            description = "Request correlation identifier.",
            example = "550e8400-e29b-41d4-a716-446655440000")
        String requestId,
    @Schema(
            description = "Response timestamp in RFC 3339 format.",
            type = "string",
            format = "date-time")
        Instant timestamp) {}
```

- [ ] **Step 6: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/api/response/
git commit -m "docs(api): add Javadoc to API response classes"
```

---

## Task 4: Add Javadoc to API Controllers

**Files:**
- Modify: `api/controller/GlobalErrorHandler.java`
- Modify: `api/controller/SampleDocumentController.java`
- Modify: `api/controller/HealthController.java`
- Modify: `api/controller/InfoController.java`
- Modify: `api/controller/RootController.java`
- Modify: `api/controller/MetricsController.java`
- Modify: `api/controller/DocumentationController.java`

- [ ] **Step 1: Add Javadoc to `GlobalErrorHandler`**

Add a class-level Javadoc comment immediately before the `@Controller` annotation:

```java
/**
 * Global Micronaut error handler that maps exceptions and HTTP status errors to RFC 9457
 * problem details wrapped in {@link ApiResponse}.
 *
 * <p>All application errors flow through this handler. Controllers must not produce ad-hoc
 * error shapes — they throw exceptions or return 4xx status codes and let this handler
 * normalize the response.
 *
 * <p>Error code assignments:
 * <ul>
 *   <li>E1001 — malformed request body (400)
 *   <li>E1002 — unsupported media type (415)
 *   <li>E1003 — constraint validation failure (422)
 *   <li>E1004 — resource not found (404)
 *   <li>E1000 — generic 4xx request error
 *   <li>E3001 — internal server error (500)
 * </ul>
 */
@Controller
public class GlobalErrorHandler {
```

- [ ] **Step 2: Add Javadoc to `SampleDocumentController`**

Add class-level Javadoc before `@Tag`:

```java
/**
 * REST controller for the sample document CRUD resource.
 *
 * <p>Provides list, get-by-id, and create operations backed by {@code SampleDocumentService}.
 * This controller exists as a boilerplate demonstration of the layered architecture and the
 * {@link ApiResponse} envelope. It does not perform any PDF or signature processing.
 *
 * <p>Route prefix is configured through {@code app.api.prefix} (default {@code /rest/v1}).
 */
@Tag(name = "Sample Documents")
@Controller("${app.api.prefix}/sample-documents")
public class SampleDocumentController {
```

- [ ] **Step 3: Add Javadoc to `HealthController`**

Add class-level Javadoc before `@Tag`:

```java
/**
 * Provides health, liveness and readiness endpoints used by Kubernetes probes and monitoring.
 *
 * <p>The full health response ({@code GET /health}) includes integration component statuses
 * collected from {@code ApplicationInfoService}. The liveness probe ({@code GET /health/live})
 * always returns UP unless the JVM is crashed. The readiness probe ({@code GET /health/ready})
 * returns {@code not_ready} when the application is draining.
 *
 * <p>The drain endpoint ({@code POST /health/drain}) triggers a controlled shutdown signal
 * without stopping the JVM, allowing Kubernetes to remove the pod from load balancer rotation
 * before the actual termination.
 */
@Tag(name = "Health")
@Controller("/health")
public class HealthController {
```

- [ ] **Step 4: Add Javadoc to `InfoController`**

Add class-level Javadoc before `@Tag`:

```java
/**
 * Exposes application version and integration readiness metadata via the REST API.
 *
 * <p>Useful for ops tooling that needs to confirm which version is deployed and which
 * integrations (storage, PDF, signatures) are enabled in the current environment.
 */
@Tag(name = "Operations")
@Controller("${app.api.prefix}/info")
public class InfoController {
```

- [ ] **Step 5: Add Javadoc to `RootController`**

Add class-level Javadoc before `@Tag`:

```java
/**
 * Root endpoint that returns a discoverable service index.
 *
 * <p>Returns basic metadata and a map of well-known endpoint paths. Useful as a starting
 * point for service discovery without prior knowledge of the API structure.
 */
@Tag(name = "Operations")
@Controller("/")
public class RootController {
```

- [ ] **Step 6: Add Javadoc to `MetricsController`**

Add class-level Javadoc before `@Tag`:

```java
/**
 * Prometheus metrics scrape endpoint.
 *
 * <p>Aggregates Micrometer metrics from the Prometheus registry and exposes them at
 * {@code GET /metrics} in the standard Prometheus text exposition format. This endpoint
 * is intended for scraping by a Prometheus server or a compatible agent.
 */
@Tag(name = "Monitoring")
@Controller("/metrics")
public class MetricsController {
```

- [ ] **Step 7: Add Javadoc to `DocumentationController`**

Add class-level Javadoc before `@Hidden`:

```java
/**
 * Serves the generated OpenAPI document and the Scalar API reference UI.
 *
 * <p>Active only when {@code app.docs.enabled} is {@code true} (the default). All responses
 * set {@code Cache-Control: no-store} so that spec changes are always reflected immediately.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /doc} — OpenAPI document as JSON
 *   <li>{@code GET /doc/openapi.json} — same
 *   <li>{@code GET /doc/openapi.json/download} — JSON with download disposition
 *   <li>{@code GET /doc/openapi.yaml} — OpenAPI document as YAML
 *   <li>{@code GET /doc/openapi.yaml/download} — YAML with download disposition
 *   <li>{@code GET /reference} — Scalar-rendered HTML reference UI
 * </ul>
 */
@Hidden
@Controller
@Requires(property = "app.docs.enabled", notEquals = "false", defaultValue = "true")
public class DocumentationController {
```

- [ ] **Step 8: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/api/controller/
git commit -m "docs(api): add Javadoc to API controllers"
```

---

## Task 5: Add Javadoc to API DTOs

**Files:**
- Modify: `api/dto/ApplicationInfoResponse.java`
- Modify: `api/dto/ApplicationComponentStatusResponse.java`
- Modify: `api/dto/CreateSampleDocumentRequest.java`
- Modify: `api/dto/SampleDocumentResponse.java`
- Modify: `api/dto/HealthFullResponse.java`
- Modify: `api/dto/HealthLivenessResponse.java`
- Modify: `api/dto/HealthReadinessResponse.java`
- Modify: `api/dto/HealthCheckResponse.java`
- Modify: `api/dto/ServiceIndexResponse.java`

Read each DTO file first with the Read tool, then add a class-level Javadoc and `@param` tags to each record. The Javadoc for each follows the same pattern as other records in this codebase.

- [ ] **Step 1: Read the DTO files to see their current content**

Use the Read tool on each of:
- `src/main/java/com/example/javastarterboilerplate/api/dto/ApplicationInfoResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/ApplicationComponentStatusResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/CreateSampleDocumentRequest.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/SampleDocumentResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/HealthFullResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/HealthLivenessResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/HealthReadinessResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/HealthCheckResponse.java`
- `src/main/java/com/example/javastarterboilerplate/api/dto/ServiceIndexResponse.java`

- [ ] **Step 2: Add Javadoc to each DTO**

For each record, add a class-level Javadoc immediately before the first annotation (or the `public record` declaration if there are none). Follow this pattern:

`ApplicationInfoResponse.java` — add before `@Serdeable`:
```java
/**
 * Response payload for {@code GET /rest/v1/info} carrying application version and
 * integration readiness information.
 *
 * @param name application name
 * @param version deployed application version
 * @param description short application description
 * @param activeDatabaseProfile active persistence profile, e.g. {@code "h2"} or {@code "postgresql"}
 * @param activeEnvironments sorted list of active Micronaut environments
 * @param integrations status of optional integrations (storage, pdfbox, dss)
 */
```

`ApplicationComponentStatusResponse.java` — add before `@Serdeable` (or `public record`):
```java
/**
 * Status of a single optional integration component reported in health and info responses.
 *
 * @param component component identifier, e.g. {@code "storage"}, {@code "pdfbox"}, {@code "dss"}
 * @param enabled whether the component is configured and ready
 * @param detail human-readable status detail from the adapter
 */
```

`CreateSampleDocumentRequest.java` — add before `@Serdeable`:
```java
/**
 * Request body for creating a new sample document record.
 *
 * @param name display name of the document; must not be blank; max 128 characters
 * @param storageKey key in the object store pointing to the document content; must not be blank
 */
```

`SampleDocumentResponse.java` — add before `@Serdeable` (or `public record`):
```java
/**
 * Response representation of a sample document resource.
 *
 * @param id unique document identifier
 * @param name display name
 * @param storageKey object store key pointing to the document content
 * @param createdAt creation timestamp in UTC
 */
```

`HealthFullResponse.java` — add before annotation or `public record`:
```java
/**
 * Full health check response returned by {@code GET /health}.
 *
 * @param status overall status string: {@code "UP"} or {@code "DOWN"}
 * @param version deployed application version
 * @param uptimeSeconds seconds elapsed since the application started
 * @param checks per-component check results
 */
```

`HealthLivenessResponse.java`:
```java
/**
 * Liveness probe response returned by {@code GET /health/live}.
 *
 * @param status always {@code "UP"} unless the JVM has crashed
 */
```

`HealthReadinessResponse.java`:
```java
/**
 * Readiness probe response returned by {@code GET /health/ready}.
 *
 * @param status {@code "ready"} when the service can accept traffic, {@code "not_ready"} during drain
 * @param checks per-component check results that informed the readiness decision
 */
```

`HealthCheckResponse.java`:
```java
/**
 * Individual component check result included in health responses.
 *
 * @param component component identifier
 * @param status {@code "UP"} or {@code "DOWN"}
 * @param order sort order hint for display purposes
 * @param detail human-readable status message from the component
 * @param details key-value map of additional diagnostic properties
 */
```

`ServiceIndexResponse.java`:
```java
/**
 * Root index response returned by {@code GET /}.
 *
 * <p>Provides a machine-readable entry point for service discovery with links to all
 * well-known operational endpoints.
 *
 * @param name application name
 * @param version deployed version
 * @param description short application description
 * @param status static {@code "UP"} string indicating the service is reachable
 * @param links map of endpoint names to their absolute paths
 */
```

- [ ] **Step 3: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/api/dto/
git commit -m "docs(api): add Javadoc to API DTOs"
```

---

## Task 6: Add Javadoc to Application Layer

**Files:**
- Modify: `application/ApplicationInfoService.java`
- Modify: `application/ApplicationShutdownState.java`
- Modify: `application/documentation/OpenApiDocumentationService.java`
- Modify: `application/sample/SampleDocumentService.java`

- [ ] **Step 1: Add Javadoc to `ApplicationInfoService`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Aggregates application metadata and integration readiness for operational endpoints.
 *
 * <p>Called by {@code InfoController} and {@code HealthController}. Reads configuration
 * properties and queries integration adapters to produce a snapshot of the service state.
 * Optional adapters ({@code ObjectStorage}) are represented as {@code Optional} to avoid
 * bean wiring failures when the adapter is disabled.
 */
@Singleton
public class ApplicationInfoService {
```

Add method Javadoc for `getInfo()` before the method:

```java
  /**
   * Returns a snapshot of application metadata and integration component statuses.
   *
   * @return application info with version, active environments and per-component status
   */
  public ApplicationInfoResponse getInfo() {
```

- [ ] **Step 2: Add Javadoc to `ApplicationShutdownState`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Thread-safe flag that signals the application is draining and should stop accepting traffic.
 *
 * <p>Set to {@code true} by {@code GracefulShutdownListener} on Micronaut {@code ShutdownEvent}.
 * Read by {@code HealthController} to flip the readiness probe to {@code not_ready}, giving
 * the Kubernetes load balancer time to drain connections before the JVM exits.
 */
@Singleton
public class ApplicationShutdownState {
```

Add method Javadoc:

```java
  /**
   * Returns {@code true} if a shutdown has been initiated.
   *
   * @return {@code true} after {@link #beginShutdown()} has been called
   */
  public boolean isShuttingDown() {
```

```java
  /**
   * Marks the application as shutting down.
   *
   * <p>This operation is idempotent and irreversible during the process lifetime.
   * Thread-safe: safe to call from any thread.
   */
  public void beginShutdown() {
```

- [ ] **Step 3: Add Javadoc to `OpenApiDocumentationService`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Loads, transforms and serves the generated OpenAPI specification.
 *
 * <p>The spec is generated at compile time by the Micronaut OpenAPI annotation processor
 * and placed at {@code META-INF/swagger/openapi.yaml} on the classpath. This service reads
 * that resource once, normalises the version field from configuration, and provides both
 * JSON and YAML representations to {@code DocumentationController}.
 *
 * <p>The parsed document is cached after the first load to avoid repeated classpath reads.
 */
@Singleton
public class OpenApiDocumentationService {
```

Add method Javadoc:

```java
  /**
   * Returns the Scalar API reference HTML page with links to the JSON and YAML endpoints.
   *
   * @return complete HTML document; never {@code null}
   */
  public String referenceHtml() {
```

```java
  /**
   * Returns the OpenAPI document as a pretty-printed JSON string.
   *
   * @return JSON string; never {@code null}
   * @throws IllegalStateException if the classpath resource is missing or unreadable
   */
  public String openApiJson() {
```

```java
  /**
   * Returns the OpenAPI document as a YAML string.
   *
   * @return YAML string; never {@code null}
   * @throws IllegalStateException if the classpath resource is missing or unreadable
   */
  public String openApiYaml() {
```

- [ ] **Step 4: Add Javadoc to `SampleDocumentService`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Application service orchestrating sample document CRUD operations.
 *
 * <p>Delegates persistence to {@code SampleDocumentRepository}. Generates document IDs and
 * creation timestamps, ensuring the domain model is always fully populated before it reaches
 * the repository layer.
 */
@Singleton
public class SampleDocumentService {
```

Add method Javadoc:

```java
  /**
   * Returns all sample documents ordered by creation time descending.
   *
   * @return unmodifiable list; never {@code null}
   */
  public List<SampleDocument> findAll() {
```

```java
  /**
   * Finds a single sample document by its identifier.
   *
   * @param id document identifier; must not be {@code null}
   * @return the document, or {@link java.util.Optional#empty()} if not found
   */
  public Optional<SampleDocument> findById(UUID id) {
```

```java
  /**
   * Creates and persists a new sample document with a generated ID and current timestamp.
   *
   * @param name display name; must not be blank
   * @param storageKey object store key for the document content; must not be blank
   * @return the persisted document with a populated {@code id} and {@code createdAt}
   */
  public SampleDocument create(String name, String storageKey) {
```

- [ ] **Step 5: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/application/
git commit -m "docs(application): add Javadoc to application service layer"
```

---

## Task 7: Add Javadoc to Config Layer

**Files:**
- Modify: `config/ApiResponseProperties.java`
- Modify: `config/ApplicationInfoProperties.java`
- Modify: `config/ClockFactory.java`
- Modify: `config/OpenApiDocumentationProperties.java`

- [ ] **Step 1: Add Javadoc to `ApiResponseProperties`**

Add class-level Javadoc before `@ConfigurationProperties`:

```java
/**
 * Configuration properties for the API response envelope, bound to the {@code api.response}
 * prefix.
 *
 * <p>Set {@code API_RESPONSE_PROBLEM_BASE_URI} (or {@code api.response.problem-base-uri}) to
 * the base URI used for RFC 9457 problem type URIs. The slug for each error type is appended
 * to this base URI, e.g. {@code https://api.example.com/problems/validation-error}.
 */
@ConfigurationProperties("api.response")
public class ApiResponseProperties {
```

Add method Javadoc:

```java
  /**
   * Returns the base URI for problem type URIs.
   *
   * @return base URI; default is {@code "https://api.example.com/problems"}
   */
  public String getProblemBaseUri() {
```

- [ ] **Step 2: Add Javadoc to `ApplicationInfoProperties`**

Add class-level Javadoc before `@ConfigurationProperties`:

```java
/**
 * Configuration properties for application identity metadata, bound to the {@code app.info}
 * prefix.
 *
 * <p>These values are injected from {@code application.yaml} (populated at build time from
 * {@code gradle.properties} via the {@code @projectVersion@} token) and are surfaced
 * through {@code GET /rest/v1/info} and health responses.
 */
@ConfigurationProperties("app.info")
public class ApplicationInfoProperties {
```

- [ ] **Step 3: Add Javadoc to `ClockFactory`**

Add class-level Javadoc before `@Factory`:

```java
/**
 * Micronaut factory that produces the application-wide {@link java.time.Clock} bean.
 *
 * <p>All timestamp generation in production code uses this injected clock rather than
 * {@code Clock.systemUTC()} directly, which allows tests to inject a fixed clock for
 * deterministic assertions.
 */
@Factory
public class ClockFactory {
```

Add method Javadoc:

```java
  /**
   * Produces a UTC system clock as a singleton bean.
   *
   * @return a clock that always returns the current UTC time
   */
  @Singleton
  public Clock utcClock() {
```

- [ ] **Step 4: Add Javadoc to `OpenApiDocumentationProperties`**

Add class-level Javadoc before `@ConfigurationProperties`:

```java
/**
 * Configuration properties for the OpenAPI documentation endpoints, bound to the
 * {@code app.docs} prefix.
 *
 * <p>Controls whether the documentation endpoints are active, the page title shown in the
 * Scalar UI, the classpath path to the generated YAML spec, and the CDN URL for the Scalar
 * JavaScript bundle. Override {@code APP_DOCS_SCALAR_SCRIPT_URL} in environments that
 * require a self-hosted or versioned CDN URL.
 */
@ConfigurationProperties("app.docs")
public class OpenApiDocumentationProperties {
```

- [ ] **Step 5: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/config/
git commit -m "docs(config): add Javadoc to configuration properties and factory classes"
```

---

## Task 8: Add Javadoc to Infrastructure Layer

**Files:**
- Modify: `infrastructure/document/PdfBoxDocumentService.java`
- Modify: `infrastructure/document/DssDigitalSignatureService.java`
- Modify: `infrastructure/persistence/SampleDocumentEntity.java`
- Modify: `infrastructure/persistence/InMemorySampleDocumentRepository.java`
- Modify: `infrastructure/persistence/SampleDocumentPersistenceAdapter.java`
- Modify: `infrastructure/persistence/PersistenceProperties.java`
- Modify: `infrastructure/storage/S3ClientFactory.java`
- Modify: `infrastructure/storage/S3ObjectStorageAdapter.java`
- Modify: `infrastructure/storage/S3StorageProperties.java`

- [ ] **Step 1: Add Javadoc to `PdfBoxDocumentService`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Infrastructure adapter implementing {@link com.example.javastarterboilerplate.domain.document.PdfDocumentService}
 * using Apache PDFBox.
 *
 * <p>Currently handles document inspection only. Future sealing and stamping operations will
 * extend this adapter as the service scope grows. The adapter is always registered regardless
 * of the {@code pdfbox.enabled} flag; the flag controls only the detail message in
 * {@link #describe()}.
 */
@Singleton
public class PdfBoxDocumentService implements PdfDocumentService {
```

Add method Javadoc for `inspect`:

```java
  /**
   * Parses the given PDF bytes and extracts page count, encryption status and version.
   *
   * @param documentBytes raw PDF bytes; must not be {@code null}
   * @return extracted metadata
   * @throws java.io.UncheckedIOException if {@code documentBytes} is not a valid PDF
   */
  @Override
  public PdfDocumentMetadata inspect(byte[] documentBytes) {
```

- [ ] **Step 2: Add Javadoc to `DssDigitalSignatureService`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Infrastructure adapter implementing {@link com.example.javastarterboilerplate.domain.document.DigitalSignatureService}
 * using the EU Digital Signature Service (DSS) library.
 *
 * <p>The current implementation is a wiring placeholder. The {@link #prepare} method wraps the
 * document in a DSS {@code InMemoryDocument} and returns metadata, but does not produce a
 * signed document. PAdES/CAdES signing workflows will replace this stub when the feature is
 * in scope.
 */
@Singleton
public class DssDigitalSignatureService implements DigitalSignatureService {
```

- [ ] **Step 3: Add Javadoc to `SampleDocumentEntity`**

Add class-level Javadoc before `@Entity`:

```java
/**
 * JPA entity mapping the {@code sample_documents} table.
 *
 * <p>Stores creation time as epoch milliseconds rather than a database timestamp type to
 * remain portable across H2, PostgreSQL and SQL Server without vendor-specific timestamp
 * handling. The conversion to {@link java.time.Instant} is done in
 * {@code SampleDocumentPersistenceAdapter}.
 */
@Entity
@Table(name = "sample_documents")
public class SampleDocumentEntity {
```

- [ ] **Step 4: Add Javadoc to `InMemorySampleDocumentRepository`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * In-memory implementation of {@link com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository},
 * active when {@code persistence.enabled} is not {@code true}.
 *
 * <p>Uses a {@link java.util.concurrent.ConcurrentHashMap} as the backing store and seeds
 * a single document on construction to enable immediate end-to-end testing without a database.
 * Data is lost when the process exits; this implementation is not suitable for production use.
 */
@Singleton
@Requires(property = "persistence.enabled", notEquals = "true", defaultValue = "false")
public class InMemorySampleDocumentRepository implements SampleDocumentRepository {
```

- [ ] **Step 5: Add Javadoc to `SampleDocumentPersistenceAdapter`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * JPA-backed implementation of {@link com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository},
 * active when {@code persistence.enabled=true}.
 *
 * <p>Bridges the domain model ({@link com.example.javastarterboilerplate.domain.sample.SampleDocument})
 * and the JPA entity ({@link SampleDocumentEntity}). Results are ordered by
 * {@code created_at_epoch_millis} descending so that the newest records are returned first.
 */
@Singleton
@Requires(property = "persistence.enabled", value = "true")
public class SampleDocumentPersistenceAdapter implements SampleDocumentRepository {
```

- [ ] **Step 6: Add Javadoc to `PersistenceProperties`**

Add class-level Javadoc before `@ConfigurationProperties`:

```java
/**
 * Configuration properties for the persistence layer, bound to the {@code persistence} prefix.
 *
 * <p>When {@code persistence.enabled} is {@code false} (the default), the in-memory repository
 * is used and no database connection is required. Set it to {@code true} and configure a
 * database profile to activate the JPA adapter.
 */
@ConfigurationProperties("persistence")
public class PersistenceProperties {
```

- [ ] **Step 7: Add Javadoc to `S3ClientFactory`**

Add class-level Javadoc before `@Factory`:

```java
/**
 * Micronaut factory that constructs the AWS SDK v2 {@link software.amazon.awssdk.services.s3.S3Client} bean.
 *
 * <p>The client is only instantiated when {@code storage.s3.enabled=true}. Supports both
 * AWS S3 and MinIO (or any S3-compatible store) via the {@code storage.s3.endpoint} override.
 * Credentials are supplied statically from {@link S3StorageProperties}; for production
 * deployments replace with IAM role-based credentials by adjusting the credentials provider.
 */
@Factory
public class S3ClientFactory {
```

- [ ] **Step 8: Add Javadoc to `S3ObjectStorageAdapter`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * AWS SDK v2 implementation of {@link com.example.javastarterboilerplate.domain.storage.ObjectStorage}.
 *
 * <p>Requires a registered {@link software.amazon.awssdk.services.s3.S3Client} bean (i.e.
 * {@code storage.s3.enabled=true}). All operations target the bucket configured in
 * {@link S3StorageProperties}. Object keys are passed through without modification.
 */
@Singleton
@Requires(bean = S3Client.class)
public class S3ObjectStorageAdapter implements ObjectStorage {
```

- [ ] **Step 9: Add Javadoc to `S3StorageProperties`**

Add class-level Javadoc before `@ConfigurationProperties`:

```java
/**
 * Configuration properties for the S3/MinIO object storage adapter, bound to the
 * {@code storage.s3} prefix.
 *
 * <p>Disabled by default ({@code storage.s3.enabled=false}). Enable via environment variable
 * {@code STORAGE_S3_ENABLED=true}. For local development with Docker Compose MinIO, the
 * default endpoint ({@code http://localhost:9000}) and credentials ({@code minioadmin}) apply.
 * Override all values for staging and production environments using environment variables.
 */
@ConfigurationProperties("storage.s3")
public class S3StorageProperties {
```

- [ ] **Step 10: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/infrastructure/
git commit -m "docs(infrastructure): add Javadoc to infrastructure adapters and properties"
```

---

## Task 9: Add Javadoc to Observability Layer and Application Entry Point

**Files:**
- Modify: `observability/RequestIdFilter.java`
- Modify: `observability/GracefulShutdownListener.java`
- Modify: `Application.java`

- [ ] **Step 1: Add Javadoc to `RequestIdFilter`**

Add class-level Javadoc before `@ServerFilter`:

```java
/**
 * Micronaut server filter that propagates request correlation identifiers across
 * the request–response lifecycle.
 *
 * <p>On each inbound request the filter reads the {@code X-Request-Id} header (falling back
 * to the legacy {@code X-Correlation-Id} header, then generating a random UUID if neither
 * is present). The resolved ID is:
 * <ul>
 *   <li>stored as a request attribute ({@link #REQUEST_ID_ATTRIBUTE}) for use by
 *       {@code ApiResponseFactory}
 *   <li>placed in the SLF4J MDC under the key {@code requestId} so that all log entries
 *       for the request carry the correlation ID
 * </ul>
 *
 * <p>On the outbound response the filter echoes the ID in both {@code X-Request-Id} and
 * the legacy {@code X-Correlation-Id} headers and removes the MDC key.
 */
@ServerFilter("/**")
public class RequestIdFilter {
```

Add field Javadoc:

```java
  /** Canonical request correlation header name. */
  public static final String HEADER_NAME = "X-Request-Id";

  /** Legacy correlation header accepted for backward compatibility. */
  public static final String LEGACY_HEADER_NAME = "X-Correlation-Id";

  /** Request attribute key under which the resolved correlation ID is stored. */
  public static final String REQUEST_ID_ATTRIBUTE = "requestId";
```

- [ ] **Step 2: Add Javadoc to `GracefulShutdownListener`**

Add class-level Javadoc before `@Singleton`:

```java
/**
 * Micronaut event listener that triggers the graceful shutdown sequence on
 * {@link io.micronaut.context.event.ShutdownEvent}.
 *
 * <p>Sets {@code ApplicationShutdownState.isShuttingDown()} to {@code true}, which causes
 * the readiness probe to return {@code not_ready}. This gives the Kubernetes load balancer
 * time to drain in-flight requests before the JVM process exits.
 */
@Singleton
public class GracefulShutdownListener {
```

- [ ] **Step 3: Add Javadoc to `Application.java`**

Add class-level Javadoc before `@OpenAPIDefinition`:

```java
/**
 * Application entry point.
 *
 * <p>Delegates CLI argument parsing to {@code ApplicationCli}. If the resolved command is
 * {@code serve} (or no arguments are given), the Micronaut runtime is started. Otherwise the
 * process exits with the code determined by the CLI handler.
 */
@OpenAPIDefinition(
```

- [ ] **Step 4: Apply formatter and run javadoc**

```bash
./gradlew spotlessApply javadoc
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/javastarterboilerplate/observability/ \
        src/main/java/com/example/javastarterboilerplate/Application.java
git commit -m "docs(observability): add Javadoc to observability classes and Application entry point"
```

---

## Task 10: Update README and Final Verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add Javadoc section to README**

In `README.md`, after the `Common commands` block (around line 94, after the closing code fence), add the following section before `## API Response Standard`:

```markdown
## Javadoc

Generate the Javadoc API documentation with:

```bash
make docs
# or equivalently:
make javadoc
# or directly:
./gradlew javadoc
```

The generated documentation is placed in `build/docs/javadoc/`. Open
`build/docs/javadoc/index.html` in a browser to browse the API.

To remove the generated output:

```bash
make clean-docs
```
```

- [ ] **Step 2: Apply Spotless to README**

```bash
./gradlew spotlessApply
```

Expected: `BUILD SUCCESSFUL` (the `misc` formatter trims trailing whitespace and ensures a final newline).

- [ ] **Step 3: Run the full check suite**

```bash
./gradlew clean check
```

Expected: `BUILD SUCCESSFUL`. All tests pass, Jacoco coverage gate passes, Spotless check passes.

- [ ] **Step 4: Regenerate Javadoc from a clean state**

```bash
./gradlew javadoc
```

Expected: `BUILD SUCCESSFUL`. Confirm `build/docs/javadoc/index.html` exists and is non-empty.

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "docs: add Javadoc generation instructions to README"
```

---

## Self-Review Checklist

**Spec coverage:**

| Requirement | Task |
|-------------|------|
| Working Makefile target for Javadoc | Task 1 |
| Single-command Javadoc generation | Task 1 |
| Gradle `javadoc` task configured | Task 1 |
| Public classes and methods have Javadoc | Tasks 2–9 |
| README instructions for Javadoc | Task 10 |
| Project builds and tests pass | Task 10 step 3 |
| No application logic changes | All tasks — only Javadoc comments added |
| `clean-docs` target | Task 1 |
| `javadoc` alias | Task 1 |
| All domain interfaces documented | Task 2 |
| All controllers documented | Task 4 |
| All application services documented | Task 6 |
| All infrastructure adapters documented | Task 8 |

**Placeholder scan:** No "TBD", "TODO", "implement later" in any task. All code blocks are complete.

**Type consistency:**
- `DigitalSignatureDescriptor`, `DigitalSignaturePreparationResult`, `PdfDocumentDescriptor`, `PdfDocumentMetadata`, `StoredObject`, `StoredObjectUpload` — all defined in Task 2 and referenced consistently in Tasks 8.
- `ApiResponse`, `ApiResponseFactory`, `ApiProblemDetails`, `ApiFieldError`, `ResponseMeta` — defined in Task 3 and cross-referenced in Task 4.
- `ApplicationShutdownState.beginShutdown()` — defined in Task 6, referenced in Task 9.
