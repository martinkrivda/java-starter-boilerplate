package com.example.javastarterboilerplate.observability;

import com.example.javastarterboilerplate.application.ApplicationShutdownState;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

/**
 * Micronaut event listener that triggers the graceful shutdown sequence on {@link
 * io.micronaut.context.event.ShutdownEvent}.
 *
 * <p>Sets {@code ApplicationShutdownState.isShuttingDown()} to {@code true}, which causes the
 * readiness probe to return {@code not_ready}. This gives the Kubernetes load balancer time to
 * drain in-flight requests before the JVM process exits.
 */
@Singleton
public class GracefulShutdownListener {

  private final ApplicationShutdownState applicationShutdownState;

  public GracefulShutdownListener(ApplicationShutdownState applicationShutdownState) {
    this.applicationShutdownState = applicationShutdownState;
  }

  @EventListener
  public void onShutdown(ShutdownEvent event) {
    applicationShutdownState.beginShutdown();
  }
}
