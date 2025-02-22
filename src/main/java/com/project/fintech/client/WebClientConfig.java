package com.project.fintech.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${brevo.api.key}")
    private String apiKey;

    /**
     * Brevo Api Web Client
     * @param builder webClient builder
     * @return web client (config for brevo)
     */
    @Bean
    public WebClient brevoWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.brevo.com/v3")
            .filter((request, next) -> {
                ClientRequest newRequest = ClientRequest.from(request)
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
                return next.exchange(newRequest);
            })
            .build();
    }
}
