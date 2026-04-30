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

@Factory
public class S3ClientFactory {

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
