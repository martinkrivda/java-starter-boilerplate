package com.example.javastarterboilerplate.infrastructure.storage;

import com.example.javastarterboilerplate.domain.storage.StoredObject;
import com.example.javastarterboilerplate.domain.storage.StoredObjectUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ObjectStorageAdapterContractTest {

    @Mock
    private S3Client s3Client;

    private S3ObjectStorageAdapter adapter;
    private S3StorageProperties properties;

    @BeforeEach
    void setUp() {
        properties = new S3StorageProperties();
        properties.setBucket("starter-documents");
        properties.setEndpoint("http://localhost:9000");
        adapter = new S3ObjectStorageAdapter(s3Client, properties);
    }

    @Test
    void createsConfigurableS3Client() {
        S3Client client = new S3ClientFactory().s3Client(properties);
        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void uploadsObjects() {
        StoredObjectUpload upload = new StoredObjectUpload("incoming/test.pdf", "application/pdf", "payload".getBytes(),
                Map.of("source", "test"));

        StoredObject storedObject = adapter.upload(upload);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertThat(requestCaptor.getValue().bucket()).isEqualTo("starter-documents");
        assertThat(requestCaptor.getValue().key()).isEqualTo("incoming/test.pdf");
        assertThat(storedObject.metadata()).containsEntry("source", "test");
    }

    @Test
    void downloadsObjectsWithMetadata() {
        byte[] content = "content".getBytes();
        GetObjectResponse response = GetObjectResponse.builder().contentType("application/pdf")
                .metadata(Map.of("source", "integration-test")).build();

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(response, content));

        StoredObject storedObject = adapter.download("incoming/test.pdf");

        assertThat(storedObject.content()).isEqualTo(content);
        assertThat(storedObject.metadata()).containsEntry("source", "integration-test");
    }

    @Test
    void downloadsObjectsWithoutMetadata() {
        byte[] content = "content".getBytes();
        GetObjectResponse response = GetObjectResponse.builder().contentType("application/pdf").build();

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(response, content));

        StoredObject storedObject = adapter.download("incoming/empty-metadata.pdf");

        assertThat(storedObject.metadata()).isEmpty();
    }

    @Test
    void deletesObjects() {
        adapter.delete("incoming/delete-me.pdf");

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().key()).isEqualTo("incoming/delete-me.pdf");
    }
}
