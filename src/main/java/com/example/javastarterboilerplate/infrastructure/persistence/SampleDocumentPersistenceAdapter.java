package com.example.javastarterboilerplate.infrastructure.persistence;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
@Requires(property = "persistence.enabled", value = "true")
public class SampleDocumentPersistenceAdapter implements SampleDocumentRepository {

  private final SampleDocumentEntityRepository repository;

  public SampleDocumentPersistenceAdapter(SampleDocumentEntityRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<SampleDocument> findAll() {
    return repository.findAllOrderedByCreatedAtEpochMillisDesc().stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<SampleDocument> findById(UUID id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public SampleDocument save(SampleDocument sampleDocument) {
    SampleDocumentEntity entity = new SampleDocumentEntity();
    entity.setId(sampleDocument.id());
    entity.setName(sampleDocument.name());
    entity.setStorageKey(sampleDocument.storageKey());
    entity.setCreatedAtEpochMillis(sampleDocument.createdAt().toEpochMilli());
    return toDomain(repository.save(entity));
  }

  private SampleDocument toDomain(SampleDocumentEntity entity) {
    return new SampleDocument(
        entity.getId(),
        entity.getName(),
        entity.getStorageKey(),
        Instant.ofEpochMilli(entity.getCreatedAtEpochMillis()));
  }
}
