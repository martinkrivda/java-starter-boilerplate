package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for the OpenAPI documentation endpoints, bound to the {@code app.docs}
 * prefix.
 *
 * <p>Controls whether the documentation endpoints are active, the page title shown in the Scalar
 * UI, the classpath path to the generated YAML spec, and the CDN URL for the Scalar JavaScript
 * bundle. Override {@code APP_DOCS_SCALAR_SCRIPT_URL} in environments that require a self-hosted or
 * versioned CDN URL.
 */
@ConfigurationProperties("app.docs")
public class OpenApiDocumentationProperties {

  private boolean enabled = true;

  private String title = "java-starter-boilerplate API reference";

  private String specResourcePath = "META-INF/swagger/openapi.yaml";

  private String openApiVersion = "3.1.1";

  private String scalarScriptUrl = "https://cdn.jsdelivr.net/npm/@scalar/api-reference";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSpecResourcePath() {
    return specResourcePath;
  }

  public void setSpecResourcePath(String specResourcePath) {
    this.specResourcePath = specResourcePath;
  }

  public String getOpenApiVersion() {
    return openApiVersion;
  }

  public void setOpenApiVersion(String openApiVersion) {
    this.openApiVersion = openApiVersion;
  }

  public String getScalarScriptUrl() {
    return scalarScriptUrl;
  }

  public void setScalarScriptUrl(String scalarScriptUrl) {
    this.scalarScriptUrl = scalarScriptUrl;
  }
}
