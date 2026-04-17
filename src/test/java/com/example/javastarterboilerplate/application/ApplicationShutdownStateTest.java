package com.example.javastarterboilerplate.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationShutdownStateTest {

    @Test
    void marksApplicationAsShuttingDown() {
        ApplicationShutdownState state = new ApplicationShutdownState();

        assertThat(state.isShuttingDown()).isFalse();

        state.beginShutdown();

        assertThat(state.isShuttingDown()).isTrue();
    }
}
