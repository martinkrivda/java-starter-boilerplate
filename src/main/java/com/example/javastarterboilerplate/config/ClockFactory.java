package com.example.javastarterboilerplate.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.time.Clock;

@Factory
public class ClockFactory {

    @Singleton
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
