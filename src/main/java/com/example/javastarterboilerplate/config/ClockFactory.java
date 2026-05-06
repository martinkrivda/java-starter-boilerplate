package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import java.time.Clock;

/**
 * Micronaut factory that produces the application-wide {@link java.time.Clock} bean.
 *
 * <p>All timestamp generation in production code uses this injected clock rather than calling
 * {@code Clock.systemUTC()} directly, which allows tests to inject a fixed clock for deterministic
 * assertions.
 */
@Factory
public class ClockFactory {

  /**
   * Produces a UTC system clock as a singleton bean.
   *
   * @return a clock that always returns the current UTC time
   */
  @Singleton
  public Clock utcClock() {
    return Clock.systemUTC();
  }
}
