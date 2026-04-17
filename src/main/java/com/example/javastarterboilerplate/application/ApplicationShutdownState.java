package com.example.javastarterboilerplate.application;

import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class ApplicationShutdownState {

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    public void beginShutdown() {
        shuttingDown.set(true);
    }
}
