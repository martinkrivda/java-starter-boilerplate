package com.example.javastarterboilerplate.infrastructure.storage;

import com.example.javastarterboilerplate.domain.storage.ObjectStorage;
import com.example.javastarterboilerplate.domain.storage.StoredObject;
import com.example.javastarterboilerplate.domain.storage.StoredObjectUpload;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.util.Map;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Singleton
@Requires(bean = S3Client.class)
public class S3ObjectStorageAdapter implements ObjectStorage {

  private final S3Client s3Client;
  private final S3StorageProperties properties;

  public S3ObjectStorageAdapter(S3Client s3Client, S3StorageProperties properties) {
    this.s3Client = s3Client;
    this.properties = properties;
  }

  @Override
  public StoredObject download(String key) {
    ResponseBytes<GetObjectResponse> response =
        s3Client.getObjectAsBytes(
            GetObjectRequest.builder().bucket(properties.getBucket()).key(key).build());

    Map<String, String> metadata = response.response().metadata();

    return new StoredObject(
        key, response.response().contentType(), response.asByteArray(), metadata);
  }

  @Override
  public StoredObject upload(StoredObjectUpload upload) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(upload.key())
            .contentType(upload.contentType())
            .metadata(upload.metadata())
            .build(),
        RequestBody.fromBytes(upload.content()));

    return new StoredObject(
        upload.key(), upload.contentType(), upload.content(), upload.metadata());
  }

  @Override
  public void delete(String key) {
    s3Client.deleteObject(
        DeleteObjectRequest.builder().bucket(properties.getBucket()).key(key).build());
  }
}
