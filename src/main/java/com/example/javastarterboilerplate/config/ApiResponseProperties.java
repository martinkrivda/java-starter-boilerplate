package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for the API response envelope, bound to the {@code api.response} prefix.
 *
 * <p>Set {@code API_RESPONSE_PROBLEM_BASE_URI} or {@code api.response.problem-base-uri} to the base
 * URI used for RFC 9457 problem type URIs. The slug for each error type is appended to this base
 * URI, e.g. {@code https://api.example.com/problems/validation-error}.
 */
@ConfigurationProperties("api.response")
public class ApiResponseProperties {

  private String problemBaseUri = "https://api.example.com/problems";

  /**
   * Returns the base URI for problem type URIs.
   *
   * @return base URI; default is {@code "https://api.example.com/problems"}
   */
  public String getProblemBaseUri() {
    return problemBaseUri;
  }

  public void setProblemBaseUri(String problemBaseUri) {
    this.problemBaseUri = problemBaseUri;
  }
}
