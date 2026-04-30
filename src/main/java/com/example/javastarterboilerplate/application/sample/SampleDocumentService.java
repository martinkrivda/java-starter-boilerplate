package com.example.javastarterboilerplate.application.sample;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class SampleDocumentService {

  private final SampleDocumentRepository sampleDocumentRepository;
  private final Clock clock;

  public SampleDocumentService(SampleDocumentRepository sampleDocumentRepository, Clock clock) {
    this.sampleDocumentRepository = sampleDocumentRepository;
    this.clock = clock;
  }

  public List<SampleDocument> findAll() {
    return sampleDocumentRepository.findAll();
  }

  public Optional<SampleDocument> findById(UUID id) {
    return sampleDocumentRepository.findById(id);
  }

  public SampleDocument create(String name, String storageKey) {
    SampleDocument sampleDocument =
        new SampleDocument(UUID.randomUUID(), name, storageKey, Instant.now(clock));
    return sampleDocumentRepository.save(sampleDocument);
  }
}
