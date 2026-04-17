package com.example.javastarterboilerplate.application.sample;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SampleDocumentServiceTest {

    @Test
    void createsAndQueriesSampleDocuments() {
        InMemorySampleDocumentRepository repository = new InMemorySampleDocumentRepository();
        Clock clock = Clock.fixed(Instant.parse("2026-04-13T10:15:30Z"), ZoneOffset.UTC);
        SampleDocumentService service = new SampleDocumentService(repository, clock);

        SampleDocument created = service.create("Sample", "incoming/sample.pdf");

        assertThat(created.name()).isEqualTo("Sample");
        assertThat(created.storageKey()).isEqualTo("incoming/sample.pdf");
        assertThat(created.createdAt()).isEqualTo(Instant.parse("2026-04-13T10:15:30Z"));
        assertThat(service.findById(created.id())).contains(created);
        assertThat(service.findAll()).containsExactly(created);
    }

    private static final class InMemorySampleDocumentRepository implements SampleDocumentRepository {

        private final List<SampleDocument> storage = new ArrayList<>();

        @Override
        public List<SampleDocument> findAll() {
            return List.copyOf(storage);
        }

        @Override
        public Optional<SampleDocument> findById(UUID id) {
            return storage.stream().filter(sampleDocument -> sampleDocument.id().equals(id)).findFirst();
        }

        @Override
        public SampleDocument save(SampleDocument sampleDocument) {
            storage.add(sampleDocument);
            return sampleDocument;
        }
    }
}
