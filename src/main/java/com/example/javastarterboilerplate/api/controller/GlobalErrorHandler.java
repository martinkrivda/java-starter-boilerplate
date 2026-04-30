package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.response.ApiFieldError;
import com.example.javastarterboilerplate.api.response.ApiResponse;
import com.example.javastarterboilerplate.api.response.ApiResponseFactory;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.exceptions.SerdeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Controller
public class GlobalErrorHandler {

  private static final Pattern INDEX_SEGMENT = Pattern.compile("\\[(\\d+)]");

  private final ApiResponseFactory responseFactory;

  public GlobalErrorHandler(ApiResponseFactory responseFactory) {
    this.responseFactory = responseFactory;
  }

  @Error(global = true, status = HttpStatus.NOT_FOUND)
  public io.micronaut.http.HttpResponse<ApiResponse<Void>> notFound(HttpRequest<?> request) {
    return responseFactory.error(
        request,
        HttpStatus.NOT_FOUND,
        "resource-not-found",
        "Resource not found",
        "Requested resource was not found.",
        "E1004",
        null);
  }

  @Error(global = true, exception = HttpStatusException.class)
  public io.micronaut.http.HttpResponse<ApiResponse<Void>> httpStatus(
      HttpRequest<?> request, HttpStatusException exception) {
    HttpStatus status = exception.getStatus();
    return responseFactory.error(
        request,
        status,
        typeSlug(status),
        title(status),
        safeDetail(status, exception),
        code(status),
        null);
  }

  @Error(global = true, exception = ConstraintViolationException.class)
  public io.micronaut.http.HttpResponse<ApiResponse<Void>> validation(
      HttpRequest<?> request, ConstraintViolationException exception) {
    List<ApiFieldError> errors =
        exception.getConstraintViolations().stream().map(this::toFieldError).toList();
    return responseFactory.error(
        request,
        HttpStatus.UNPROCESSABLE_ENTITY,
        "validation-error",
        "Validation error",
        "Request is not valid.",
        "E1003",
        errors);
  }

  @Error(global = true, exception = SerdeException.class)
  public io.micronaut.http.HttpResponse<ApiResponse<Void>> malformedJson(
      HttpRequest<?> request, SerdeException exception) {
    return responseFactory.error(
        request,
        HttpStatus.BAD_REQUEST,
        "malformed-request",
        "Bad request",
        "Request body is not a valid JSON document.",
        "E1001",
        null);
  }

  @Error(global = true, exception = Throwable.class)
  public io.micronaut.http.HttpResponse<ApiResponse<Void>> unexpected(
      HttpRequest<?> request, Throwable exception) {
    return responseFactory.error(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "internal-error",
        "Internal server error",
        "An internal error occurred. Please try again later.",
        "E3001",
        null);
  }

  private ApiFieldError toFieldError(ConstraintViolation<?> violation) {
    String pointer = toJsonPointer(violation);
    return new ApiFieldError(
        pointer, fieldName(pointer), validationCode(violation), violation.getMessage());
  }

  private String toJsonPointer(ConstraintViolation<?> violation) {
    String path = violation.getPropertyPath().toString();
    List<String> rawSegments = List.of(path.split("\\."));
    int startIndex = rawSegments.size() > 2 ? 2 : Math.max(rawSegments.size() - 1, 0);
    return rawSegments.subList(startIndex, rawSegments.size()).stream()
        .flatMap(segment -> expandSegment(segment).stream())
        .map(this::escapeJsonPointerSegment)
        .reduce("", (pointer, segment) -> pointer + "/" + segment);
  }

  private List<String> expandSegment(String segment) {
    java.util.regex.Matcher matcher = INDEX_SEGMENT.matcher(segment);
    java.util.ArrayList<String> segments = new java.util.ArrayList<>();
    String normalized = segment.replaceAll("\\[(\\d+)]", "");
    if (!normalized.isBlank()) {
      segments.add(normalized);
    }
    while (matcher.find()) {
      segments.add(matcher.group(1));
    }
    return segments;
  }

  private String escapeJsonPointerSegment(String segment) {
    return segment.replace("~", "~0").replace("/", "~1");
  }

  private String fieldName(String pointer) {
    if (pointer == null || pointer.isBlank()) {
      return null;
    }
    String[] segments = pointer.split("/");
    return segments[segments.length - 1];
  }

  private String validationCode(ConstraintViolation<?> violation) {
    Class<?> annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType();
    String simpleName = annotationType.getSimpleName();
    if ("NotNull".equals(simpleName)
        || "NotBlank".equals(simpleName)
        || "NotEmpty".equals(simpleName)) {
      return "REQUIRED";
    }
    if ("Email".equals(simpleName) || "Pattern".equals(simpleName)) {
      return "INVALID_FORMAT";
    }
    if ("Size".equals(simpleName)) {
      return "INVALID_LENGTH";
    }
    if ("Min".equals(simpleName)
        || "Positive".equals(simpleName)
        || "PositiveOrZero".equals(simpleName)) {
      return "MIN_VALUE";
    }
    return simpleName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
  }

  private String typeSlug(HttpStatus status) {
    if (status == HttpStatus.BAD_REQUEST) {
      return "bad-request";
    }
    if (status == HttpStatus.NOT_FOUND) {
      return "resource-not-found";
    }
    if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
      return "unsupported-media-type";
    }
    return status.getCode() >= 500 ? "internal-error" : "request-error";
  }

  private String title(HttpStatus status) {
    if (status == HttpStatus.BAD_REQUEST) {
      return "Bad request";
    }
    if (status == HttpStatus.NOT_FOUND) {
      return "Resource not found";
    }
    if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
      return "Unsupported media type";
    }
    return status.getCode() >= 500 ? "Internal server error" : "Request error";
  }

  private String safeDetail(HttpStatus status, HttpStatusException exception) {
    if (status.getCode() >= 500) {
      return "An internal error occurred. Please try again later.";
    }
    return exception.getMessage();
  }

  private String code(HttpStatus status) {
    if (status == HttpStatus.BAD_REQUEST) {
      return "E1001";
    }
    if (status == HttpStatus.NOT_FOUND) {
      return "E1004";
    }
    if (status == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
      return "E1002";
    }
    return status.getCode() >= 500 ? "E3001" : "E1000";
  }
}
