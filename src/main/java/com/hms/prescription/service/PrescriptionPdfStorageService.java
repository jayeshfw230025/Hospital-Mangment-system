package com.hms.prescription.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores generated prescription PDFs on local disk - same placeholder pattern as
 * Module 7's ReportFileStorageService, pending the MinIO integration from Stage A.
 */
@Service
public class PrescriptionPdfStorageService {

    private final Path storageRoot;

    public PrescriptionPdfStorageService(@Value("${hms.prescription.pdf-storage-path:./data/prescription-pdfs}") String storagePath) {
        this.storageRoot = Path.of(storagePath);
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize prescription PDF storage directory", e);
        }
    }

    public String store(byte[] pdfBytes, String suggestedFileName) {
        String storageKey = UUID.randomUUID() + "-" + suggestedFileName;
        try {
            Files.write(storageRoot.resolve(storageKey), pdfBytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store prescription PDF", e);
        }
        return storageKey;
    }

    public Resource load(String storageKey) {
        return new FileSystemResource(storageRoot.resolve(storageKey));
    }
}
