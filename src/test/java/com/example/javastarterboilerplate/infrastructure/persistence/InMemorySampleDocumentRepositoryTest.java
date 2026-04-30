package com.example.javastarterboilerplate.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemorySampleDocumentRepositoryTest {

  private final InMemorySampleDocumentRepository repository =
      new InMemorySampleDocumentRepository();

  @Test
  void readsSeededDocumentAndPersistsNewDocuments() {
    assertThat(repository.findAll())
        .extracting(SampleDocument::name)
        .contains("Seeded in-memory sample");

    SampleDocument sampleDocument =
        new SampleDocument(
            UUID.randomUUID(),
            "In-memory sample",
            "incoming/in-memory-sample.pdf",
            Instant.parse("2026-04-13T13:00:00Z"));

    SampleDocument saved = repository.save(sampleDocument);

    assertThat(repository.findById(saved.id())).contains(saved);
    assertThat(repository.findAll()).first().isEqualTo(saved);
  }
}
