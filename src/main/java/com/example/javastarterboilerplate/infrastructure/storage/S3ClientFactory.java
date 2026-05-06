package com.example.javastarterboilerplate.infrastructure.storage;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Micronaut factory that constructs the AWS SDK v2 {@link
 * software.amazon.awssdk.services.s3.S3Client} bean.
 *
 * <p>The client is only instantiated when {@code storage.s3.enabled=true}. Supports both AWS S3 and
 * MinIO, or any S3-compatible store, via the {@code storage.s3.endpoint} override. Credentials are
 * supplied statically from {@link S3StorageProperties}; for production deployments replace with IAM
 * role-based credentials by adjusting the credentials provider.
 */
@Factory
public class S3ClientFactory {

  /**
   * Builds an S3 client configured from application properties.
   *
   * @param properties S3/MinIO configuration properties
   * @return configured AWS SDK S3 client
   */
  @Singleton
  @Requires(property = "storage.s3.enabled", value = "true")
  public S3Client s3Client(S3StorageProperties properties) {
    var builder =
        S3Client.builder()
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getAccessKey(), properties.getSecretKey())))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(properties.isPathStyleAccess())
                    .build());

    if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(properties.getEndpoint()));
    }

    return builder.build();
  }
}
