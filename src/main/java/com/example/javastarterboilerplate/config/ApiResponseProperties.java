package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("api.response")
public class ApiResponseProperties {

  private String problemBaseUri = "https://api.example.com/problems";

  public String getProblemBaseUri() {
    return problemBaseUri;
  }

  public void setProblemBaseUri(String problemBaseUri) {
    this.problemBaseUri = problemBaseUri;
  }
}
