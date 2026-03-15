package com.learning.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public FileStorageService(
            @Value("${file.upload-dir:./uploads}") String uploadDir,
            @Value("${file.base-url:http://localhost:8080}") String baseUrl) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.stripTrailing();
        Files.createDirectories(this.uploadDir);
    }

    /**
     * 儲存檔案並回傳可存取的 URL
     */
    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";

        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/uploads/" + filename;
    }
}
