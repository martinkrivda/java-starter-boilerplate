package com.example.javastarterboilerplate.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "test", transactional = false)
class SampleDocumentPersistenceAdapterTest {

  @Inject SampleDocumentRepository sampleDocumentRepository;

  @Test
  void loadsSeededDataFromFlywayMigration() {
    assertThat(sampleDocumentRepository.findAll())
        .extracting(SampleDocument::name)
        .contains("Seeded H2 sample");
  }

  @Test
  void savesAndReadsDomainDocuments() {
    SampleDocument sampleDocument =
        new SampleDocument(
            UUID.randomUUID(),
            "Persisted sample",
            "archive/persisted-sample.pdf",
            Instant.parse("2026-04-13T12:34:56Z"));

    SampleDocument saved = sampleDocumentRepository.save(sampleDocument);

    assertThat(sampleDocumentRepository.findById(saved.id())).contains(saved);
  }
}
