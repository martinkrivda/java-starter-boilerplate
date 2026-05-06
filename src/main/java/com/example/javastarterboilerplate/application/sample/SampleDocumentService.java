package com.example.javastarterboilerplate.application.sample;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service orchestrating sample document CRUD operations.
 *
 * <p>Delegates persistence to {@code SampleDocumentRepository}. Generates document IDs and creation
 * timestamps, ensuring the domain model is always fully populated before it reaches the repository
 * layer.
 */
@Singleton
public class SampleDocumentService {

  private final SampleDocumentRepository sampleDocumentRepository;
  private final Clock clock;

  public SampleDocumentService(SampleDocumentRepository sampleDocumentRepository, Clock clock) {
    this.sampleDocumentRepository = sampleDocumentRepository;
    this.clock = clock;
  }

  /**
   * Returns all sample documents ordered by creation time descending.
   *
   * @return unmodifiable list; never {@code null}
   */
  public List<SampleDocument> findAll() {
    return sampleDocumentRepository.findAll();
  }

  /**
   * Finds a single sample document by its identifier.
   *
   * @param id document identifier; must not be {@code null}
   * @return the document, or {@link java.util.Optional#empty()} if not found
   */
  public Optional<SampleDocument> findById(UUID id) {
    return sampleDocumentRepository.findById(id);
  }

  /**
   * Creates and persists a new sample document with a generated ID and current timestamp.
   *
   * @param name display name; must not be blank
   * @param storageKey object store key for the document content; must not be blank
   * @return the persisted document with a populated {@code id} and {@code createdAt}
   */
  public SampleDocument create(String name, String storageKey) {
    SampleDocument sampleDocument =
        new SampleDocument(UUID.randomUUID(), name, storageKey, Instant.now(clock));
    return sampleDocumentRepository.save(sampleDocument);
  }
}
