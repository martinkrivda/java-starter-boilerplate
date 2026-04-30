package com.example.javastarterboilerplate.infrastructure.persistence;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

@Repository
@Requires(property = "persistence.enabled", value = "true")
public interface SampleDocumentEntityRepository extends CrudRepository<SampleDocumentEntity, UUID> {

  @Query(
      "SELECT sampleDocument FROM SampleDocumentEntity AS sampleDocument ORDER BY sampleDocument.createdAtEpochMillis DESC")
  List<SampleDocumentEntity> findAllOrderedByCreatedAtEpochMillisDesc();
}
