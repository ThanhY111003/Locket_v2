package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // 1. Cấu hình Dotenv để đọc file .env ở thư mục gốc
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // Tránh lỗi nếu lỡ quên file .env
                .load();

        // 2. Đưa các biến từ .env vào System Properties
        // Điều này giúp Spring Boot nhận diện được các biến ${VAR} trong
        // application.properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        // 3. Chạy ứng dụng Spring Boot
        SpringApplication.run(DemoApplication.class, args);
    }

}