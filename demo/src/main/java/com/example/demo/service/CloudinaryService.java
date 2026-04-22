package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CloudinaryService {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    // Thư mục lưu ảnh local khi Cloudinary chưa cấu hình
    @Value("${upload.local-dir:uploads}")
    private String localUploadDir;

    // Base URL của server để tạo URL truy cập ảnh
    @Value("${server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

    private Cloudinary cloudinary;
    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (isValidCredential(cloudName) && isValidCredential(apiKey) && isValidCredential(apiSecret)) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            configured = true;
            log.info("✅ Cloudinary initialized with cloud: {}", cloudName);
        } else {
            configured = false;
            log.warn("⚠️  Cloudinary chưa cấu hình → ảnh sẽ được lưu local tại /{}", localUploadDir);
            // Tạo thư mục uploads nếu chưa có
            try {
                Files.createDirectories(Paths.get(localUploadDir));
            } catch (IOException e) {
                log.error("Không thể tạo thư mục uploads: {}", e.getMessage());
            }
        }
    }

    /**
     * Upload ảnh:
     * - Nếu Cloudinary đã cấu hình → upload lên cloud, trả về Cloudinary URL
     * - Nếu chưa cấu hình → lưu vào thư mục local, trả về URL localhost
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) {
        if (!configured) {
            return saveLocally(file);
        }

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", "locket_finance",
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto"
            );
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            String url = (String) result.get("secure_url");
            log.debug("✅ Image uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Upload Cloudinary thất bại: {}. Dùng local fallback.", e.getMessage());
            return saveLocally(file);
        } catch (Exception e) {
            log.error("Cloudinary lỗi: {}. Dùng local fallback.", e.getMessage());
            return saveLocally(file);
        }
    }

    /**
     * Lưu ảnh vào thư mục local và trả về URL truy cập qua server.
     */
    private String saveLocally(MultipartFile file) {
        try {
            // Giữ đuôi file gốc (jpg, png, webp...)
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String filename = UUID.randomUUID() + ext;
            Path uploadPath = Paths.get(localUploadDir).resolve(filename);
            Files.write(uploadPath, file.getBytes());

            String url = serverBaseUrl + "/uploads/" + filename;
            log.info("📁 Image saved locally: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Không thể lưu ảnh local: {}", e.getMessage());
            throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa ảnh theo publicId (Cloudinary) hoặc filename (local).
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null) return;

        if (configured && !imageUrl.contains("localhost")) {
            // Cloudinary: extract public_id từ URL
            try {
                String publicId = extractPublicId(imageUrl);
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                log.warn("Không thể xóa ảnh Cloudinary: {}", imageUrl);
            }
        } else if (imageUrl.contains("localhost")) {
            // Local: xóa file
            try {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path path = Paths.get(localUploadDir).resolve(filename);
                Files.deleteIfExists(path);
                log.debug("Đã xóa ảnh local: {}", path);
            } catch (IOException e) {
                log.warn("Không thể xóa ảnh local: {}", imageUrl);
            }
        }
    }

    public boolean isConfigured() {
        return configured;
    }

    // ===== Helpers =====

    private boolean isValidCredential(String value) {
        return value != null
                && !value.isBlank()
                && !value.startsWith("your_")
                && !value.equals("YOUR_VALUE");
    }

    private String extractPublicId(String cloudinaryUrl) {
        // URL dạng: https://res.cloudinary.com/cloud/image/upload/v123/folder/filename.jpg
        // public_id = folder/filename (không có extension)
        int uploadIndex = cloudinaryUrl.indexOf("/upload/");
        if (uploadIndex == -1) return cloudinaryUrl;
        String afterUpload = cloudinaryUrl.substring(uploadIndex + 8); // bỏ "/upload/"
        // Bỏ version (v123/)
        if (afterUpload.startsWith("v") && afterUpload.indexOf("/") > 0) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // Bỏ extension
        int dotIndex = afterUpload.lastIndexOf(".");
        return dotIndex > 0 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}
