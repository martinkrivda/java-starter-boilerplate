package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("app.info")
public class ApplicationInfoProperties {

  private String name = "java-starter-boilerplate";

  private String version = "0.1.0-SNAPSHOT";

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
