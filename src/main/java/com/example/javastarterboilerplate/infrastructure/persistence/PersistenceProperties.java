package com.example.javastarterboilerplate.infrastructure.persistence;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("persistence")
public class PersistenceProperties {

  private boolean enabled = false;

  private String vendor = "none";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }
}
