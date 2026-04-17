package com.example.javastarterboilerplate.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sample_documents")
public class SampleDocumentEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "storage_key", nullable = false, length = 256)
    private String storageKey;

    @Column(name = "created_at_epoch_millis", nullable = false)
    private Long createdAtEpochMillis;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public Long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }

    public void setCreatedAtEpochMillis(Long createdAtEpochMillis) {
        this.createdAtEpochMillis = createdAtEpochMillis;
    }
}
