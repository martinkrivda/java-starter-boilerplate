package com.example.javastarterboilerplate.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationShutdownStateTest {

  @Test
  void marksApplicationAsShuttingDown() {
    ApplicationShutdownState state = new ApplicationShutdownState();

    assertThat(state.isShuttingDown()).isFalse();

    state.beginShutdown();

    assertThat(state.isShuttingDown()).isTrue();
  }
}
