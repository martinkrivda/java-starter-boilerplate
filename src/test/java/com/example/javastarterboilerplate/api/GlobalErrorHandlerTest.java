package com.example.javastarterboilerplate.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.api.controller.GlobalErrorHandler;
import com.example.javastarterboilerplate.api.response.ApiResponse;
import com.example.javastarterboilerplate.api.response.ApiResponseFactory;
import com.example.javastarterboilerplate.config.ApiResponseProperties;
import com.example.javastarterboilerplate.observability.RequestIdFilter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.exceptions.SerdeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GlobalErrorHandlerTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-04-14T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void buildsNotFoundMalformedAndUnexpectedErrors() {
    GlobalErrorHandler handler =
        new GlobalErrorHandler(factory("https://api.example.com/problems"));
    MutableHttpRequest<?> request = request("request-1", "/api/test");

    ApiResponse<Void> notFound = handler.notFound(request).body();
    ApiResponse<Void> malformed =
        handler.malformedJson(request, new SerdeException("bad json")).body();
    ApiResponse<Void> unexpected =
        handler.unexpected(request, new IllegalStateException("boom")).body();

    assertThat(notFound.error().type())
        .isEqualTo("https://api.example.com/problems/resource-not-found");
    assertThat(notFound.error().code()).isEqualTo("E1004");
    assertThat(malformed.error().type())
        .isEqualTo("https://api.example.com/problems/malformed-request");
    assertThat(malformed.error().code()).isEqualTo("E1001");
    assertThat(unexpected.error().type())
        .isEqualTo("https://api.example.com/problems/internal-error");
    assertThat(unexpected.error().detail())
        .isEqualTo("An internal error occurred. Please try again later.");
  }

  @Test
  void mapsHttpStatusExceptionsToProblemDetails() {
    GlobalErrorHandler handler =
        new GlobalErrorHandler(factory("https://api.example.com/problems"));
    MutableHttpRequest<?> request = request("request-2", "/api/test");

    ApiResponse<Void> badRequest =
        handler
            .httpStatus(request, new HttpStatusException(HttpStatus.BAD_REQUEST, "bad request"))
            .body();
    ApiResponse<Void> unsupported =
        handler
            .httpStatus(
                request,
                new HttpStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported media"))
            .body();
    ApiResponse<Void> generic =
        handler
            .httpStatus(request, new HttpStatusException(HttpStatus.TOO_MANY_REQUESTS, "slow down"))
            .body();
    ApiResponse<Void> server =
        handler
            .httpStatus(
                request, new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE, "dependency down"))
            .body();

    assertThat(badRequest.error().type()).isEqualTo("https://api.example.com/problems/bad-request");
    assertThat(badRequest.error().title()).isEqualTo("Bad request");
    assertThat(badRequest.error().code()).isEqualTo("E1001");
    assertThat(unsupported.error().type())
        .isEqualTo("https://api.example.com/problems/unsupported-media-type");
    assertThat(unsupported.error().title()).isEqualTo("Unsupported media type");
    assertThat(unsupported.error().code()).isEqualTo("E1002");
    assertThat(generic.error().type()).isEqualTo("https://api.example.com/problems/request-error");
    assertThat(generic.error().title()).isEqualTo("Request error");
    assertThat(generic.error().code()).isEqualTo("E1000");
    assertThat(generic.error().detail()).isEqualTo("slow down");
    assertThat(server.error().type()).isEqualTo("https://api.example.com/problems/internal-error");
    assertThat(server.error().title()).isEqualTo("Internal server error");
    assertThat(server.error().code()).isEqualTo("E3001");
    assertThat(server.error().detail())
        .isEqualTo("An internal error occurred. Please try again later.");
  }

  @Test
  void convertsConstraintViolationsToJsonPointerErrors() {
    GlobalErrorHandler handler =
        new GlobalErrorHandler(factory("https://api.example.com/problems"));
    MutableHttpRequest<?> request = request("request-3", "/rest/v1/sample-documents");
    ConstraintViolationException exception =
        new ConstraintViolationException(
            Set.of(
                violation("create.request.items[0].name", NotBlank.class, "must not be blank"),
                violation("create.request.items[1].code", Pattern.class, "must match"),
                violation(
                    "create.request.items[0].quantity", Positive.class, "must be greater than 0"),
                violation("title", Size.class, "size must be between 2 and 2147483647")));

    ApiResponse<Void> response = handler.validation(request, exception).body();

    assertThat(response.error().type())
        .isEqualTo("https://api.example.com/problems/validation-error");
    assertThat(response.error().errors())
        .extracting("pointer")
        .contains("/items/0/name", "/items/1/code", "/items/0/quantity", "/title");
    assertThat(response.error().errors())
        .extracting("code")
        .contains("REQUIRED", "INVALID_FORMAT", "MIN_VALUE", "INVALID_LENGTH");
    assertThat(response.error().requestId()).isEqualTo("request-3");
    assertThat(response.meta().timestamp()).isEqualTo(Instant.parse("2026-04-14T12:00:00Z"));
  }

  @Test
  void handlesUnknownConstraintCodesAndBlankPointers() {
    GlobalErrorHandler handler =
        new GlobalErrorHandler(factory("https://api.example.com/problems"));
    MutableHttpRequest<?> request = request("request-4", "/api/test");
    ConstraintViolationException exception =
        new ConstraintViolationException(
            Set.of(violation("", Deprecated.class, "deprecated path")));

    ApiResponse<Void> response = handler.validation(request, exception).body();

    assertThat(response.error().errors())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.pointer()).isEqualTo("");
              assertThat(item.field()).isNull();
              assertThat(item.code()).isEqualTo("DEPRECATED");
            });
  }

  private ApiResponseFactory factory(String problemBaseUri) {
    ApiResponseProperties properties = new ApiResponseProperties();
    properties.setProblemBaseUri(problemBaseUri);
    return new ApiResponseFactory(clock, properties);
  }

  private MutableHttpRequest<?> request(String requestId, String path) {
    MutableHttpRequest<?> request = HttpRequest.GET(path);
    request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, requestId);
    return request;
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<Object> violation(
      String propertyPath, Class<? extends Annotation> annotationType, String message) {
    ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
    Path path = Mockito.mock(Path.class);
    ConstraintDescriptor<?> descriptor = Mockito.mock(ConstraintDescriptor.class);
    Annotation annotation = Mockito.mock(annotationType);

    Mockito.when(path.toString()).thenReturn(propertyPath);
    Mockito.when(descriptor.getAnnotation()).thenReturn(annotation);
    Mockito.when(annotation.annotationType()).thenReturn((Class) annotationType);
    Mockito.when(violation.getPropertyPath()).thenReturn(path);
    Mockito.when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);
    Mockito.when(violation.getMessage()).thenReturn(message);

    return violation;
  }
}
