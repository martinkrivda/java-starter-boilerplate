package com.example.javastarterboilerplate.infrastructure.persistence;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for the persistence layer, bound to the {@code persistence} prefix.
 *
 * <p>When {@code persistence.enabled} is {@code false}, the default, the in-memory repository is
 * used and no database connection is required. Set it to {@code true} and configure a database
 * profile to activate the JPA adapter.
 */
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
