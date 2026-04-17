package com.example.javastarterboilerplate.infrastructure.persistence;

import com.example.javastarterboilerplate.domain.sample.SampleDocument;
import com.example.javastarterboilerplate.domain.sample.SampleDocumentRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
@Requires(property = "persistence.enabled", notEquals = "true", defaultValue = "false")
public class InMemorySampleDocumentRepository implements SampleDocumentRepository {

    private static final SampleDocument SEEDED_DOCUMENT = new SampleDocument(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "Seeded in-memory sample",
            "incoming/seeded-in-memory.pdf",
            Instant.parse("2026-04-13T12:00:00Z"));

    private final ConcurrentMap<UUID, SampleDocument> storage = new ConcurrentHashMap<>();

    public InMemorySampleDocumentRepository() {
        storage.put(SEEDED_DOCUMENT.id(), SEEDED_DOCUMENT);
    }

    @Override
    public List<SampleDocument> findAll() {
        return storage.values().stream().sorted(Comparator.comparing(SampleDocument::createdAt).reversed()).toList();
    }

    @Override
    public Optional<SampleDocument> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public SampleDocument save(SampleDocument sampleDocument) {
        storage.put(sampleDocument.id(), sampleDocument);
        return sampleDocument;
    }
}
