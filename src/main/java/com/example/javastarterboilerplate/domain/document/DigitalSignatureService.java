package com.example.javastarterboilerplate.domain.document;

/**
 * Domain contract for digital signature operations.
 *
 * <p>Implementations live in the {@code infrastructure} layer. The current adapter wires EU Digital
 * Signature Service integration points for future PAdES/CAdES workflows. The domain layer remains
 * free of framework and vendor dependencies.
 */
public interface DigitalSignatureService {

  /**
   * Returns a human-readable descriptor identifying the active signature provider.
   *
   * @return provider identity and readiness detail; never {@code null}
   */
  DigitalSignatureDescriptor describe();

  /**
   * Prepares a document for signing without performing the actual signing operation.
   *
   * <p>This is a placeholder method for the future signing workflow. The current implementation
   * inspects the document and returns metadata required by the caller to initiate signing.
   *
   * @param documentBytes raw PDF document bytes; must not be {@code null} or empty
   * @param fileName original file name including extension, used as the document name
   * @return preparation result containing provider, file name, byte size and MIME type
   */
  DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName);
}
