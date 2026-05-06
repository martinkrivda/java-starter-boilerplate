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
 * Centralized factory for building {@link ApiResponse}-wrapped HTTP responses.
 *
 * <p>Controllers delegate response construction to this factory to guarantee consistent envelope
 * structure, metadata population, and problem-type URI generation across every endpoint.
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
   * <p>The response body sets {@code success=false} and populates the {@code error} field. A {@code
   * Cache-Control: no-store} header is added to prevent caching of error responses.
   *
   * @param request incoming request
   * @param status HTTP status for the error
   * @param typeSlug last path segment appended to the problem base URI to form the type URI
   * @param title short human-readable problem title
   * @param detail safe human-readable error detail, avoiding internal state for 5xx responses
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
