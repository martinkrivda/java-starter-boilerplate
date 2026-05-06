package com.example.javastarterboilerplate.application;

import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe flag that signals the application is draining and should stop accepting traffic.
 *
 * <p>Set to {@code true} by {@code GracefulShutdownListener} on Micronaut shutdown events. Read by
 * {@code HealthController} to flip the readiness probe to {@code not_ready}, giving the Kubernetes
 * load balancer time to drain connections before the JVM exits.
 */
@Singleton
public class ApplicationShutdownState {

  private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

  /**
   * Returns {@code true} if a shutdown has been initiated.
   *
   * @return {@code true} after {@link #beginShutdown()} has been called
   */
  public boolean isShuttingDown() {
    return shuttingDown.get();
  }

  /**
   * Marks the application as shutting down.
   *
   * <p>This operation is idempotent and irreversible during the process lifetime. It is safe to
   * call from any thread.
   */
  public void beginShutdown() {
    shuttingDown.set(true);
  }
}
