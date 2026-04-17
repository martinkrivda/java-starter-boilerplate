package com.example.javastarterboilerplate.contracts;

import com.example.javastarterboilerplate.config.ApplicationInfoProperties;
import com.example.javastarterboilerplate.config.ApiResponseProperties;
import com.example.javastarterboilerplate.config.ClockFactory;
import com.example.javastarterboilerplate.config.OpenApiDocumentationProperties;
import com.example.javastarterboilerplate.infrastructure.document.DssProperties;
import com.example.javastarterboilerplate.infrastructure.document.PdfBoxProperties;
import com.example.javastarterboilerplate.infrastructure.persistence.PersistenceProperties;
import com.example.javastarterboilerplate.infrastructure.persistence.SampleDocumentEntity;
import com.example.javastarterboilerplate.infrastructure.storage.S3StorageProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MutableTypeContractTest {

    @Test
    void exposesMutableContracts() {
        ApplicationInfoProperties infoProperties = new ApplicationInfoProperties();
        infoProperties.setName("starter");
        infoProperties.setVersion("1.2.3");
        infoProperties.setDescription("description");
        assertThat(infoProperties.getName()).isEqualTo("starter");
        assertThat(infoProperties.getVersion()).isEqualTo("1.2.3");
        assertThat(infoProperties.getDescription()).isEqualTo("description");

        ApiResponseProperties apiResponseProperties = new ApiResponseProperties();
        apiResponseProperties.setProblemBaseUri("https://api.test/problems");
        assertThat(apiResponseProperties.getProblemBaseUri()).isEqualTo("https://api.test/problems");

        OpenApiDocumentationProperties documentationProperties = new OpenApiDocumentationProperties();
        documentationProperties.setEnabled(false);
        documentationProperties.setTitle("API reference");
        documentationProperties.setSpecResourcePath("META-INF/swagger/openapi.yaml");
        documentationProperties.setOpenApiVersion("3.1.1");
        documentationProperties.setScalarScriptUrl("https://cdn.example.test/scalar.js");
        assertThat(documentationProperties.isEnabled()).isFalse();
        assertThat(documentationProperties.getTitle()).isEqualTo("API reference");
        assertThat(documentationProperties.getSpecResourcePath()).isEqualTo("META-INF/swagger/openapi.yaml");
        assertThat(documentationProperties.getOpenApiVersion()).isEqualTo("3.1.1");
        assertThat(documentationProperties.getScalarScriptUrl()).isEqualTo("https://cdn.example.test/scalar.js");

        DssProperties dssProperties = new DssProperties();
        dssProperties.setEnabled(false);
        dssProperties.setRoadmapNote("roadmap");
        assertThat(dssProperties.isEnabled()).isFalse();
        assertThat(dssProperties.getRoadmapNote()).isEqualTo("roadmap");

        PdfBoxProperties pdfBoxProperties = new PdfBoxProperties();
        pdfBoxProperties.setEnabled(false);
        pdfBoxProperties.setRoadmapNote("pdf roadmap");
        assertThat(pdfBoxProperties.isEnabled()).isFalse();
        assertThat(pdfBoxProperties.getRoadmapNote()).isEqualTo("pdf roadmap");

        PersistenceProperties persistenceProperties = new PersistenceProperties();
        persistenceProperties.setVendor("sqlserver");
        assertThat(persistenceProperties.getVendor()).isEqualTo("sqlserver");

        S3StorageProperties s3StorageProperties = new S3StorageProperties();
        s3StorageProperties.setEnabled(true);
        s3StorageProperties.setEndpoint("http://minio:9000");
        s3StorageProperties.setRegion("eu-west-1");
        s3StorageProperties.setBucket("bucket");
        s3StorageProperties.setPathStyleAccess(false);
        s3StorageProperties.setAccessKey("access");
        s3StorageProperties.setSecretKey("secret");
        assertThat(s3StorageProperties.isEnabled()).isTrue();
        assertThat(s3StorageProperties.getEndpoint()).isEqualTo("http://minio:9000");
        assertThat(s3StorageProperties.getRegion()).isEqualTo("eu-west-1");
        assertThat(s3StorageProperties.getBucket()).isEqualTo("bucket");
        assertThat(s3StorageProperties.isPathStyleAccess()).isFalse();
        assertThat(s3StorageProperties.getAccessKey()).isEqualTo("access");
        assertThat(s3StorageProperties.getSecretKey()).isEqualTo("secret");

        SampleDocumentEntity entity = new SampleDocumentEntity();
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-04-13T12:00:00Z");
        entity.setId(id);
        entity.setName("entity");
        entity.setStorageKey("archive/entity.pdf");
        entity.setCreatedAtEpochMillis(createdAt.toEpochMilli());
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("entity");
        assertThat(entity.getStorageKey()).isEqualTo("archive/entity.pdf");
        assertThat(entity.getCreatedAtEpochMillis()).isEqualTo(createdAt.toEpochMilli());

        assertThat(new ClockFactory().utcClock().getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
