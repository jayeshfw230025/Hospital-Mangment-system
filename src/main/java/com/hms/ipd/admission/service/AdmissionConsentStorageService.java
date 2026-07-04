package com.hms.ipd.admission.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores uploaded admission consent documents on local disk - same placeholder
 * pattern as Modules 7/8's file storage services, pending the MinIO integration
 * from Stage A.
 */
@Service
public class AdmissionConsentStorageService {

    private final Path storageRoot;

    public AdmissionConsentStorageService(@Value("${hms.admission.consent-storage-path:./data/admission-consents}") String storagePath) {
        this.storageRoot = Path.of(storagePath);
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize consent document storage directory", e);
        }
    }

    public String store(MultipartFile file) {
        String storageKey = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        try {
            Files.copy(file.getInputStream(), storageRoot.resolve(storageKey));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store consent document", e);
        }
        return storageKey;
    }

    private String sanitize(String filename) {
        if (filename == null) {
            return "consent";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
