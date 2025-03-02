package com.project.fintech.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "TokenPairDto", requiredProperties = {"accessToken", "refreshToken"})
public class TokenPairDto {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDA4NDYwOT...")
    private String accessToken;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDA4NDYwOT...")
    private String refreshToken;
}