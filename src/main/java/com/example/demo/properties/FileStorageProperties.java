package com.example.demo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;

    private String uploadChunk;

    public String getUploadChunk() {
        return uploadChunk;
    }

    public void setUploadChunk(String uploadChunk) {
        this.uploadChunk = uploadChunk;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
