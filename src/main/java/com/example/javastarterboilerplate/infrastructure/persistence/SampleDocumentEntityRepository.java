package com.example.javastarterboilerplate.infrastructure.persistence;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

/**
 * Micronaut Data repository for {@link SampleDocumentEntity} rows.
 *
 * <p>Active only when persistence is enabled. Custom query methods keep ordering behavior explicit
 * for the JPA-backed sample document adapter.
 */
@Repository
@Requires(property = "persistence.enabled", value = "true")
public interface SampleDocumentEntityRepository extends CrudRepository<SampleDocumentEntity, UUID> {

  /**
   * Returns all sample document entities ordered from newest to oldest.
   *
   * @return entities ordered by {@code createdAtEpochMillis} descending
   */
  @Query(
      "SELECT sampleDocument FROM SampleDocumentEntity AS sampleDocument ORDER BY sampleDocument.createdAtEpochMillis DESC")
  List<SampleDocumentEntity> findAllOrderedByCreatedAtEpochMillisDesc();
}
