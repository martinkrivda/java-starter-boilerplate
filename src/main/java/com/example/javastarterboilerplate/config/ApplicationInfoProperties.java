package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for application identity metadata, bound to the {@code app.info} prefix.
 *
 * <p>These values are injected from {@code application.yaml}, populated at build time from {@code
 * gradle.properties} and CI build metadata tokens, and surfaced through {@code GET /rest/v1/info}
 * and health responses.
 */
@ConfigurationProperties("app.info")
public class ApplicationInfoProperties {

  private String name = "java-starter-boilerplate";

  private String version = "0.1.0-SNAPSHOT";

  private String buildNumber = "local";

  private String buildCommit = "unknown";

  private String buildTimestamp = "unknown";

  private String description = "Micronaut starter for document sealing and signing services";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getBuildNumber() {
    return buildNumber;
  }

  public void setBuildNumber(String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getBuildCommit() {
    return buildCommit;
  }

  public void setBuildCommit(String buildCommit) {
    this.buildCommit = buildCommit;
  }

  public String getBuildTimestamp() {
    return buildTimestamp;
  }

  public void setBuildTimestamp(String buildTimestamp) {
    this.buildTimestamp = buildTimestamp;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
