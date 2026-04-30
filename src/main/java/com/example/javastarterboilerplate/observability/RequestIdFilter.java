package com.example.javastarterboilerplate.observability;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import java.util.UUID;
import org.slf4j.MDC;

@ServerFilter("/**")
public class RequestIdFilter {

  public static final String HEADER_NAME = "X-Request-Id";
  public static final String LEGACY_HEADER_NAME = "X-Correlation-Id";
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
