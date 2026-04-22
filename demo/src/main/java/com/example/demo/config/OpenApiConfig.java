package com.example.demo.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer Token — Nhập token sau khi login (không cần 'Bearer ' prefix)",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Locket Finance API")
                        .description("Backend REST API cho ứng dụng theo dõi chi tiêu qua ảnh — Locket Finance")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Locket Finance Team")
                                .email("dev@locketfinance.app")));
    }
}
