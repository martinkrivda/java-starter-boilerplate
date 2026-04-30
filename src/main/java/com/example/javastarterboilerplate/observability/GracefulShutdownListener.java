package com.example.javastarterboilerplate.observability;

import com.example.javastarterboilerplate.application.ApplicationShutdownState;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

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
