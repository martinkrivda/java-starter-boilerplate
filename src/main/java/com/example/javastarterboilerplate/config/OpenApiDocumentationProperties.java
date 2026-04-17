package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

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
