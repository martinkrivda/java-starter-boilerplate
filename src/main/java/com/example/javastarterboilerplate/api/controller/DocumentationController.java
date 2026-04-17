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

@Hidden
@Controller
@Requires(property = "app.docs.enabled", notEquals = "false", defaultValue = "true")
public class DocumentationController {

    private static final MediaType YAML_MEDIA_TYPE = MediaType.of("application/yaml");

    private final OpenApiDocumentationService documentationService;

    public DocumentationController(OpenApiDocumentationService documentationService) {
        this.documentationService = documentationService;
    }

    @Get(uri = "/doc", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> reference() {
        return html(documentationService.referenceHtml());
    }

    @Get(uri = "/reference", produces = MediaType.TEXT_HTML)
    public HttpResponse<String> legacyReferenceRoute() {
        return html(documentationService.referenceHtml());
    }

    @Get(uri = "/doc/openapi.json", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<String> openApiJson() {
        return content(MediaType.APPLICATION_JSON_TYPE, documentationService.openApiJson());
    }

    @Get(uri = "/doc/openapi.json/download", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<String> downloadOpenApiJson() {
        return downloadable("openapi.json", MediaType.APPLICATION_JSON_TYPE, documentationService.openApiJson());
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
        return HttpResponse.ok(content).contentType(mediaType).header(HttpHeaders.CACHE_CONTROL, "no-store");
    }

    private MutableHttpResponse<String> downloadable(String filename, MediaType mediaType, @NotBlank String content) {
        return content(mediaType, content).header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
    }
}
