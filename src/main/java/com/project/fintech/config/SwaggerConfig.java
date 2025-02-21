package com.project.fintech.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "💳 Fintech Service",  // Swagger 제목
        version = "1.0",  // API 버전
        description = "fintech service API docs." // 설명
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "로컬 서버"),
    }
)
public class SwaggerConfig {

}
