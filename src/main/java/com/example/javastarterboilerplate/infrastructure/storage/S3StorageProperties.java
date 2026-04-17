package com.example.javastarterboilerplate.infrastructure.storage;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("storage.s3")
public class S3StorageProperties {

    private boolean enabled = false;

    private String endpoint = "http://localhost:9000";

    private String region = "eu-central-1";

    private String bucket = "starter-documents";

    private boolean pathStyleAccess = true;

    private String accessKey = "minioadmin";

    private String secretKey = "minioadmin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
