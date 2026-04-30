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

@Singleton
public class ApiResponseFactory {

  private final Clock clock;
  private final ApiResponseProperties properties;

  public ApiResponseFactory(Clock clock, ApiResponseProperties properties) {
    this.clock = clock;
    this.properties = properties;
  }

  public <T> ApiResponse<T> success(HttpRequest<?> request, T data) {
    return ApiResponse.success(data, meta(request));
  }

  public <T> MutableHttpResponse<ApiResponse<T>> ok(HttpRequest<?> request, T data) {
    return HttpResponse.ok(success(request, data));
  }

  public <T> MutableHttpResponse<ApiResponse<T>> created(
      HttpRequest<?> request, T data, URI location) {
    return HttpResponse.created(success(request, data))
        .headers(headers -> headers.location(location));
  }

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
