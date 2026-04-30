package com.example.javastarterboilerplate.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.config.ApplicationInfoProperties;
import com.example.javastarterboilerplate.domain.document.DigitalSignatureDescriptor;
import com.example.javastarterboilerplate.domain.document.DigitalSignaturePreparationResult;
import com.example.javastarterboilerplate.domain.document.DigitalSignatureService;
import com.example.javastarterboilerplate.domain.document.PdfDocumentDescriptor;
import com.example.javastarterboilerplate.domain.document.PdfDocumentMetadata;
import com.example.javastarterboilerplate.domain.document.PdfDocumentService;
import com.example.javastarterboilerplate.domain.storage.ObjectStorage;
import com.example.javastarterboilerplate.domain.storage.StoredObject;
import com.example.javastarterboilerplate.domain.storage.StoredObjectUpload;
import com.example.javastarterboilerplate.infrastructure.document.DssProperties;
import com.example.javastarterboilerplate.infrastructure.document.PdfBoxProperties;
import com.example.javastarterboilerplate.infrastructure.persistence.PersistenceProperties;
import com.example.javastarterboilerplate.infrastructure.storage.S3StorageProperties;
import io.micronaut.context.env.Environment;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ApplicationInfoServiceTest {

  @Test
  void reportsEnabledStorageWhenAdapterIsPresent() {
    ApplicationInfoProperties infoProperties = new ApplicationInfoProperties();
    PersistenceProperties persistenceProperties = new PersistenceProperties();
    persistenceProperties.setEnabled(true);
    persistenceProperties.setVendor("postgresql");

    S3StorageProperties s3StorageProperties = new S3StorageProperties();
    s3StorageProperties.setEnabled(true);
    s3StorageProperties.setBucket("sealed-documents");

    PdfBoxProperties pdfBoxProperties = new PdfBoxProperties();
    pdfBoxProperties.setEnabled(true);

    DssProperties dssProperties = new DssProperties();
    dssProperties.setEnabled(true);

    ObjectStorage objectStorage =
        new ObjectStorage() {
          @Override
          public StoredObject download(String key) {
            return new StoredObject(key, "application/pdf", new byte[0], Map.of());
          }

          @Override
          public StoredObject upload(StoredObjectUpload upload) {
            return new StoredObject(
                upload.key(), upload.contentType(), upload.content(), upload.metadata());
          }

          @Override
          public void delete(String key) {}
        };

    PdfDocumentService pdfDocumentService =
        new PdfDocumentService() {
          @Override
          public PdfDocumentDescriptor describe() {
            return new PdfDocumentDescriptor("pdfbox", "ready");
          }

          @Override
          public PdfDocumentMetadata inspect(byte[] documentBytes) {
            return new PdfDocumentMetadata(1, false, 1.7f);
          }
        };

    DigitalSignatureService digitalSignatureService =
        new DigitalSignatureService() {
          @Override
          public DigitalSignatureDescriptor describe() {
            return new DigitalSignatureDescriptor("dss", "ready");
          }

          @Override
          public DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName) {
            return new DigitalSignaturePreparationResult(
                "dss", fileName, documentBytes.length, "application/pdf");
          }
        };

    Environment environment = Mockito.mock(Environment.class);
    Mockito.when(environment.getActiveNames()).thenReturn(java.util.Set.of("dev", "postgresql"));

    ApplicationInfoService service =
        new ApplicationInfoService(
            infoProperties,
            persistenceProperties,
            s3StorageProperties,
            pdfBoxProperties,
            dssProperties,
            Optional.of(objectStorage),
            pdfDocumentService,
            digitalSignatureService,
            environment);

    ApplicationInfoResponse info = service.getInfo();

    assertThat(info.activeDatabaseProfile()).isEqualTo("postgresql");
    assertThat(info.activeEnvironments()).containsExactly("dev", "postgresql");
    assertThat(info.integrations())
        .anySatisfy(
            component -> {
              assertThat(component.component()).isEqualTo("storage");
              assertThat(component.enabled()).isTrue();
              assertThat(component.detail()).contains("sealed-documents");
            });
  }

  @Test
  void reportsDisabledStorageWhenAdapterIsMissing() {
    ApplicationInfoProperties infoProperties = new ApplicationInfoProperties();
    PersistenceProperties persistenceProperties = new PersistenceProperties();

    S3StorageProperties s3StorageProperties = new S3StorageProperties();
    s3StorageProperties.setEnabled(false);

    PdfBoxProperties pdfBoxProperties = new PdfBoxProperties();
    DssProperties dssProperties = new DssProperties();

    PdfDocumentService pdfDocumentService =
        new PdfDocumentService() {
          @Override
          public PdfDocumentDescriptor describe() {
            return new PdfDocumentDescriptor("pdfbox", "ready");
          }

          @Override
          public PdfDocumentMetadata inspect(byte[] documentBytes) {
            return new PdfDocumentMetadata(1, false, 1.7f);
          }
        };

    DigitalSignatureService digitalSignatureService =
        new DigitalSignatureService() {
          @Override
          public DigitalSignatureDescriptor describe() {
            return new DigitalSignatureDescriptor("dss", "ready");
          }

          @Override
          public DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName) {
            return new DigitalSignaturePreparationResult(
                "dss", fileName, documentBytes.length, "application/pdf");
          }
        };

    Environment environment = Mockito.mock(Environment.class);
    Mockito.when(environment.getActiveNames()).thenReturn(java.util.Set.of("test"));

    ApplicationInfoService service =
        new ApplicationInfoService(
            infoProperties,
            persistenceProperties,
            s3StorageProperties,
            pdfBoxProperties,
            dssProperties,
            Optional.empty(),
            pdfDocumentService,
            digitalSignatureService,
            environment);

    ApplicationInfoResponse info = service.getInfo();

    assertThat(info.activeDatabaseProfile()).isEqualTo("none");
    assertThat(info.integrations())
        .anySatisfy(
            component -> {
              assertThat(component.component()).isEqualTo("storage");
              assertThat(component.enabled()).isFalse();
              assertThat(component.detail()).isEqualTo("disabled");
            });
  }
}
