package com.hms.investigation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores investigation report files on local disk. Placeholder for the MinIO
 * (S3-compatible) object storage from Stage A, which has not been implemented yet -
 * swapping this out later only requires changing store()/load() internals, since
 * callers only depend on the opaque storage key this returns.
 */
@Service
public class ReportFileStorageService {

    private final Path storageRoot;

    public ReportFileStorageService(@Value("${hms.investigation.report-storage-path:./data/investigation-reports}") String storagePath) {
        this.storageRoot = Path.of(storagePath);
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize report storage directory", e);
        }
    }

    public String store(MultipartFile file) {
        String storageKey = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        try {
            Files.copy(file.getInputStream(), storageRoot.resolve(storageKey));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store report file", e);
        }
        return storageKey;
    }

    public Resource load(String storageKey) {
        return new FileSystemResource(storageRoot.resolve(storageKey));
    }

    private String sanitize(String filename) {
        if (filename == null) {
            return "report";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
