package com.learning.api.service.Chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    private static final Set<String> IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> VIDEO_TYPES = Set.of(
        "video/mp4", "video/webm", "video/quicktime"
    );
    private static final Set<String> AUDIO_TYPES = Set.of(
        "audio/mpeg", "audio/wav", "audio/ogg", "audio/webm"
    );

    /**
     * 通用儲存邏輯
     * @param file   檔案
     * @param subDir 子目錄 (例如 "avatars", "chat", "videos")
     * @return 完整的可存取 URL
     */
    public String store(MultipartFile file, String subDir) {
        if (file.isEmpty()) throw new IllegalArgumentException("檔案不可為空");

        // 1. 取得副檔名並生成唯一檔名
        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        String filename = UUID.randomUUID() + ext;

        // 2. 建立目錄
        Path targetDir = Paths.get(uploadDir, subDir);
        try {
            Files.createDirectories(targetDir);

            // 3. 儲存檔案
            Path targetPath = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("檔案儲存失敗", e);
        }

        // 4. 回傳統一格式的 URL
        return baseUrl + "/uploads/" + subDir + "/" + filename;
    }

    /**
     * 專門給老師個人檔案用的輔助方法
     */
    public String saveImage(MultipartFile file) {
        validateType(file, IMAGE_TYPES);
        return store(file, "images");
    }

    public String saveVideo(MultipartFile file) {
        validateType(file, VIDEO_TYPES);
        return store(file, "videos");
    }

    /**
     * 刪除實體檔案（用於更新個人資料時清理舊圖）
     */
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/uploads/")) return;
        try {
            // 從 URL 反推磁碟路徑
            String relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 9);
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("刪除舊檔案失敗: " + e.getMessage());
        }
    }

    /**
     * 根據檔案的 Content-Type 推斷 messageType
     * 4=IMAGE, 5=VIDEO, 3=VOICE, 6=FILE
     */
    public int detectMessageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return 6;
        if (IMAGE_TYPES.contains(contentType)) return 4;
        if (VIDEO_TYPES.contains(contentType)) return 5;
        if (AUDIO_TYPES.contains(contentType)) return 3;
        return 6;
    }

    /**
     * 載入檔案資源（供下載使用）
     */
    public org.springframework.core.io.Resource loadAsResource(String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            return new org.springframework.core.io.UrlResource(file.toUri());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("無法載入檔案: " + filename, e);
        }
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    private void validateType(MultipartFile file, Set<String> allowed) {
        if (!allowed.contains(file.getContentType())) {
            throw new IllegalArgumentException("不支援的格式: " + file.getContentType());
        }
    }
}
