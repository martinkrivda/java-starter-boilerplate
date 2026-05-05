package com.example.javastarterboilerplate.observability;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import java.util.UUID;
import org.slf4j.MDC;

/**
 * Micronaut server filter that propagates request correlation identifiers across the
 * request-response lifecycle.
 *
 * <p>On each inbound request the filter reads the {@code X-Request-Id} header, falling back to the
 * legacy {@code X-Correlation-Id} header, then generating a random UUID if neither is present. The
 * resolved ID is:
 *
 * <ul>
 *   <li>stored as a request attribute ({@link #REQUEST_ID_ATTRIBUTE}) for use by {@code
 *       ApiResponseFactory}
 *   <li>placed in the SLF4J MDC under the key {@code requestId} so that request log entries carry
 *       the correlation ID
 * </ul>
 *
 * <p>On the outbound response the filter echoes the ID in both {@code X-Request-Id} and the legacy
 * {@code X-Correlation-Id} headers and removes the MDC key.
 */
@ServerFilter("/**")
public class RequestIdFilter {

  /** Canonical request correlation header name. */
  public static final String HEADER_NAME = "X-Request-Id";

  /** Legacy correlation header accepted for backward compatibility. */
  public static final String LEGACY_HEADER_NAME = "X-Correlation-Id";

  /** Request attribute key under which the resolved correlation ID is stored. */
  public static final String REQUEST_ID_ATTRIBUTE = "requestId";

  private static final String MDC_KEY = "requestId";

  @RequestFilter
  public void onRequest(MutableHttpRequest<?> request) {
    String requestId =
        request
            .getHeaders()
            .get(HEADER_NAME, String.class)
            .orElseGet(
                () ->
                    request
                        .getHeaders()
                        .get(LEGACY_HEADER_NAME, String.class)
                        .orElseGet(() -> UUID.randomUUID().toString()));
    request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
    MDC.put(MDC_KEY, requestId);
  }

  @ResponseFilter
  public void onResponse(HttpRequest<?> request, MutableHttpResponse<?> response) {
    String requestId = request.getAttribute(REQUEST_ID_ATTRIBUTE, String.class).orElse("not-set");
    response.getHeaders().set(HEADER_NAME, requestId);
    response.getHeaders().set(LEGACY_HEADER_NAME, requestId);
    MDC.remove(MDC_KEY);
  }
}
