package com.example.javastarterboilerplate.domain.document;

public interface DigitalSignatureService {

    DigitalSignatureDescriptor describe();

    DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName);
}
