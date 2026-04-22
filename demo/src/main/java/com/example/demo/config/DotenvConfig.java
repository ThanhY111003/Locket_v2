package com.example.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Đọc file {@code .env} ở thư mục gốc của project và nạp vào Spring Environment
 * TRƯỚC KHI application context được khởi tạo.
 *
 * <p>Cần đăng ký class này trong {@code spring.factories} hoặc
 * {@code META-INF/spring/ApplicationContextInitializer} để Spring Boot
 * tự động gọi khi khởi động.</p>
 *
 * <p>Thứ tự ưu tiên: System ENV > .env file > default trong application.properties</p>
 */
@Slf4j
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Tìm .env tại thư mục gốc project (cùng cấp với pom.xml)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")          // Thư mục chạy app (root của project)
                    .filename(".env")         // Tên file
                    .ignoreIfMissing()        // Không crash nếu file không tồn tại
                    .load();

            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                // Chỉ set nếu chưa có trong System properties (System > .env)
                if (System.getenv(entry.getKey()) == null) {
                    envMap.put(entry.getKey(), entry.getValue());
                }
            });

            if (!envMap.isEmpty()) {
                // Thêm với priority thấp hơn System env nhưng cao hơn application.properties
                applicationContext.getEnvironment()
                        .getPropertySources()
                        .addLast(new MapPropertySource("dotenvProperties", envMap));

                log.info("Loaded {} properties from .env file", envMap.size());
            } else {
                log.info("No .env file found or file is empty, using system environment variables");
            }

        } catch (DotenvException e) {
            log.warn("Could not load .env file: {}. Falling back to system environment variables.", e.getMessage());
        }
    }
}
