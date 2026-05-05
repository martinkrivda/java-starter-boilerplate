package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.application.documentation.OpenApiDocumentationService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotBlank;

/**
 * Serves the generated OpenAPI document and the Scalar API reference UI.
 *
 * <p>Active only when {@code app.docs.enabled} is {@code true}, the default. All specification
 * responses set {@code Cache-Control: no-store} so that spec changes are always reflected
 * immediately.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>{@code GET /doc} - OpenAPI document as JSON
 *   <li>{@code GET /doc/openapi.json} - same
 *   <li>{@code GET /doc/openapi.json/download} - JSON with download disposition
 *   <li>{@code GET /doc/openapi.yaml} - OpenAPI document as YAML
 *   <li>{@code GET /doc/openapi.yaml/download} - YAML with download disposition
 *   <li>{@code GET /reference} - Scalar-rendered HTML reference UI
 * </ul>
 */
@Hidden
@Controller
@Requires(property = "app.docs.enabled", notEquals = "false", defaultValue = "true")
public class DocumentationController {

  private static final MediaType YAML_MEDIA_TYPE = MediaType.of("application/yaml");

  private final OpenApiDocumentationService documentationService;

  public DocumentationController(OpenApiDocumentationService documentationService) {
    this.documentationService = documentationService;
  }

  @Get(uri = "/doc", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> openApiDocument() {
    return content(MediaType.APPLICATION_JSON_TYPE, documentationService.openApiJson());
  }

  @Get(uri = "/reference", produces = MediaType.TEXT_HTML)
  public HttpResponse<String> reference() {
    return html(documentationService.referenceHtml());
  }

  @Get(uri = "/doc/openapi.json", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> openApiJson() {
    return content(MediaType.APPLICATION_JSON_TYPE, documentationService.openApiJson());
  }

  @Get(uri = "/doc/openapi.json/download", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> downloadOpenApiJson() {
    return downloadable(
        "openapi.json", MediaType.APPLICATION_JSON_TYPE, documentationService.openApiJson());
  }

  @Get(uri = "/doc/openapi.yaml", produces = "application/yaml")
  public HttpResponse<String> openApiYaml() {
    return content(YAML_MEDIA_TYPE, documentationService.openApiYaml());
  }

  @Get(uri = "/doc/openapi.yaml/download", produces = "application/yaml")
  public HttpResponse<String> downloadOpenApiYaml() {
    return downloadable("openapi.yaml", YAML_MEDIA_TYPE, documentationService.openApiYaml());
  }

  private MutableHttpResponse<String> html(@NotBlank String content) {
    return HttpResponse.ok(content).contentType(MediaType.TEXT_HTML_TYPE);
  }

  private MutableHttpResponse<String> content(MediaType mediaType, @NotBlank String content) {
    return HttpResponse.ok(content)
        .contentType(mediaType)
        .header(HttpHeaders.CACHE_CONTROL, "no-store");
  }

  private MutableHttpResponse<String> downloadable(
      String filename, MediaType mediaType, @NotBlank String content) {
    return content(mediaType, content)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
  }
}
