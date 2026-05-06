package com.example.javastarterboilerplate.domain.document;

/**
 * Identifies the active digital signature provider and its readiness state.
 *
 * @param provider short provider identifier, e.g. {@code "dss"}
 * @param detail human-readable status detail returned by the adapter
 */
public record DigitalSignatureDescriptor(String provider, String detail) {}
