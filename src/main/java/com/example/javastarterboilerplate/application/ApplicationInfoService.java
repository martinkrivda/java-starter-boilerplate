package com.example.javastarterboilerplate.application;

import com.example.javastarterboilerplate.api.dto.ApplicationComponentStatusResponse;
import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.config.ApplicationInfoProperties;
import com.example.javastarterboilerplate.domain.document.DigitalSignatureService;
import com.example.javastarterboilerplate.domain.document.PdfDocumentService;
import com.example.javastarterboilerplate.domain.storage.ObjectStorage;
import com.example.javastarterboilerplate.infrastructure.document.DssProperties;
import com.example.javastarterboilerplate.infrastructure.document.PdfBoxProperties;
import com.example.javastarterboilerplate.infrastructure.persistence.PersistenceProperties;
import com.example.javastarterboilerplate.infrastructure.storage.S3StorageProperties;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
public class ApplicationInfoService {

  private final ApplicationInfoProperties applicationInfoProperties;
  private final PersistenceProperties persistenceProperties;
  private final S3StorageProperties s3StorageProperties;
  private final PdfBoxProperties pdfBoxProperties;
  private final DssProperties dssProperties;
  private final Optional<ObjectStorage> objectStorage;
  private final PdfDocumentService pdfDocumentService;
  private final DigitalSignatureService digitalSignatureService;
  private final Environment environment;

  public ApplicationInfoService(
      ApplicationInfoProperties applicationInfoProperties,
      PersistenceProperties persistenceProperties,
      S3StorageProperties s3StorageProperties,
      PdfBoxProperties pdfBoxProperties,
      DssProperties dssProperties,
      Optional<ObjectStorage> objectStorage,
      PdfDocumentService pdfDocumentService,
      DigitalSignatureService digitalSignatureService,
      Environment environment) {
    this.applicationInfoProperties = applicationInfoProperties;
    this.persistenceProperties = persistenceProperties;
    this.s3StorageProperties = s3StorageProperties;
    this.pdfBoxProperties = pdfBoxProperties;
    this.dssProperties = dssProperties;
    this.objectStorage = objectStorage;
    this.pdfDocumentService = pdfDocumentService;
    this.digitalSignatureService = digitalSignatureService;
    this.environment = environment;
  }

  public ApplicationInfoResponse getInfo() {
    List<ApplicationComponentStatusResponse> integrations = new ArrayList<>();
    integrations.add(
        new ApplicationComponentStatusResponse(
            "storage",
            s3StorageProperties.isEnabled() && objectStorage.isPresent(),
            s3StorageProperties.isEnabled()
                ? "S3/MinIO adapter ready for bucket " + s3StorageProperties.getBucket()
                : "disabled"));
    integrations.add(
        new ApplicationComponentStatusResponse(
            "pdfbox", pdfBoxProperties.isEnabled(), pdfDocumentService.describe().detail()));
    integrations.add(
        new ApplicationComponentStatusResponse(
            "dss", dssProperties.isEnabled(), digitalSignatureService.describe().detail()));

    List<String> activeEnvironments =
        environment.getActiveNames().stream().sorted(Comparator.naturalOrder()).toList();

    return new ApplicationInfoResponse(
        applicationInfoProperties.getName(),
        applicationInfoProperties.getVersion(),
        applicationInfoProperties.getDescription(),
        persistenceProperties.getVendor(),
        activeEnvironments,
        integrations);
  }
}
