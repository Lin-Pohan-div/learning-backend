package com.learning.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");
    private static final Set<String> AUDIO_TYPES = Set.of("audio/mpeg", "audio/wav", "audio/ogg", "audio/webm");

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("無法建立上傳目錄: " + this.uploadDir, e);
        }
    }

    public String store(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());
        return "/uploads/" + filename;
    }

    public Resource loadAsResource(String filename) throws MalformedURLException {
        Path file = uploadDir.resolve(filename).normalize();
        return new UrlResource(file.toUri());
    }

    /**
     * 根據檔案的 Content-Type 推斷 messageType：4=IMAGE, 5=VIDEO, 3=VOICE, 6=FILE
     */
    public int detectMessageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return 6;
        if (IMAGE_TYPES.contains(contentType)) return 4;
        if (VIDEO_TYPES.contains(contentType)) return 5;
        if (AUDIO_TYPES.contains(contentType)) return 3;
        return 6;
    }
}
