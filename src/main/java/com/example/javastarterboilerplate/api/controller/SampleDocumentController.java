package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.dto.CreateSampleDocumentRequest;
import com.example.javastarterboilerplate.api.dto.SampleDocumentResponse;
import com.example.javastarterboilerplate.api.response.ApiResponse;
import com.example.javastarterboilerplate.api.response.ApiResponseFactory;
import com.example.javastarterboilerplate.application.sample.SampleDocumentService;
import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Tag(name = "Sample Documents")
@Controller("${app.api.prefix}/sample-documents")
public class SampleDocumentController {

  private final SampleDocumentService sampleDocumentService;
  private final ApiResponseFactory responseFactory;

  public SampleDocumentController(
      SampleDocumentService sampleDocumentService, ApiResponseFactory responseFactory) {
    this.sampleDocumentService = sampleDocumentService;
    this.responseFactory = responseFactory;
  }

  @Get
  @Operation(
      summary = "List sample documents",
      description = "Returns boilerplate sample document records from the persistence adapter.")
  public ApiResponse<List<SampleDocumentResponse>> list(HttpRequest<?> request) {
    return responseFactory.success(
        request, sampleDocumentService.findAll().stream().map(this::toResponse).toList());
  }

  @Get("/{id}")
  @Operation(
      summary = "Get a sample document",
      description = "Loads a single boilerplate sample document by identifier.")
  public ApiResponse<SampleDocumentResponse> get(HttpRequest<?> request, UUID id) {
    SampleDocumentResponse response =
        sampleDocumentService
            .findById(id)
            .map(this::toResponse)
            .orElseThrow(
                () ->
                    new HttpStatusException(
                        io.micronaut.http.HttpStatus.NOT_FOUND,
                        "Requested resource was not found."));
    return responseFactory.success(request, response);
  }

  @Post
  @Operation(
      summary = "Create a sample document",
      description =
          "Creates a boilerplate sample record without applying any PDF sealing or signature workflow.")
  public HttpResponse<ApiResponse<SampleDocumentResponse>> create(
      HttpRequest<?> httpRequest, @Body @Valid CreateSampleDocumentRequest request) {
    SampleDocument created = sampleDocumentService.create(request.name(), request.storageKey());
    return responseFactory.created(
        httpRequest,
        toResponse(created),
        java.net.URI.create(httpRequest.getPath() + "/" + created.id()));
  }

  private SampleDocumentResponse toResponse(SampleDocument sampleDocument) {
    return new SampleDocumentResponse(
        sampleDocument.id(),
        sampleDocument.name(),
        sampleDocument.storageKey(),
        sampleDocument.createdAt());
  }
}
