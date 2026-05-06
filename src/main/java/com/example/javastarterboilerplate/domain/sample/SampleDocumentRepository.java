package com.example.javastarterboilerplate.domain.sample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for {@link SampleDocument} persistence.
 *
 * <p>Implementations are in the {@code infrastructure} layer. Two adapters exist: {@code
 * InMemorySampleDocumentRepository} (used when {@code persistence.enabled=false}) and {@code
 * SampleDocumentPersistenceAdapter} backed by JPA (used when {@code persistence.enabled=true}).
 */
public interface SampleDocumentRepository {

  /**
   * Returns all sample documents, ordered by creation time descending.
   *
   * @return unmodifiable list; never {@code null}
   */
  List<SampleDocument> findAll();

  /**
   * Finds a single sample document by its identifier.
   *
   * @param id document identifier; must not be {@code null}
   * @return the document, or {@link java.util.Optional#empty()} if not found
   */
  Optional<SampleDocument> findById(UUID id);

  /**
   * Persists a new or updated document.
   *
   * @param sampleDocument document to save; must not be {@code null}
   * @return the saved document, which may be the same instance or a new object
   */
  SampleDocument save(SampleDocument sampleDocument);
}
