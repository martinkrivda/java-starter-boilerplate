package com.example.javastarterboilerplate.domain.sample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SampleDocumentRepository {

    List<SampleDocument> findAll();

    Optional<SampleDocument> findById(UUID id);

    SampleDocument save(SampleDocument sampleDocument);
}
