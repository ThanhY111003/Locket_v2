package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Gọi Google Gemini API để phân tích ảnh và gợi ý danh mục chi tiêu.
 *
 * <p>Nếu {@code gemini.api-key} không được cấu hình, service sẽ trả về {@code null}
 * và hệ thống sẽ bỏ qua bước phân loại AI.</p>
 */
@Service
@Slf4j
public class GeminiService {

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private static final String PROMPT =
            "Phân tích hình ảnh này và xác định danh mục chi tiêu phù hợp nhất từ danh sách sau: " +
            "[Ăn uống, Mua sắm, Di chuyển, Giải trí, Hóa đơn, Khác]. " +
            "Chỉ trả về đúng tên danh mục, không giải thích thêm.";

    @Value("${gemini.api-key:}")
    private String apiKey;

    private final RestClient restClient;

    public GeminiService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    /**
     * Phân tích ảnh từ URL và gợi ý danh mục chi tiêu.
     *
     * @param imageUrl URL của ảnh (từ Cloudinary)
     * @return Tên danh mục được gợi ý, hoặc {@code null} nếu không thể phân tích
     */
    public String categorizeExpense(String imageUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Gemini API key not configured, skipping AI categorization");
            return null;
        }

        try {
            // Bước 1: Tải ảnh về dưới dạng bytes
            byte[] imageBytes = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);

            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Could not download image from URL: {}", imageUrl);
                return null;
            }

            // Bước 2: Encode sang Base64
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Bước 3: Xây dựng request body cho Gemini API
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", PROMPT),
                                    Map.of("inlineData", Map.of(
                                            "mimeType", "image/jpeg",
                                            "data", base64Image
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "maxOutputTokens", 20,
                            "temperature", 0.1
                    )
            );

            // Bước 4: Gọi Gemini API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(GEMINI_API_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // Bước 5: Parse kết quả
            if (response != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content =
                            (Map<String, Object>) candidates.get(0).get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts =
                            (List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        String suggestion = ((String) parts.get(0).get("text")).trim();
                        log.info("Gemini suggested category: '{}'", suggestion);
                        return suggestion;
                    }
                }
            }

        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
        }

        return null;
    }
}
