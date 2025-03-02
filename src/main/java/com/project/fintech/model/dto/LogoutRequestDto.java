package com.project.fintech.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LogoutRequestDto", description = "로그인 api 호출 시 필요한 DTO")
public class LogoutRequestDto {

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDA4NDYwOT...")
    @NotBlank(message = "Access Token 값이 누락되었습니다.")
    private String accessToken;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDA4NDYwOT...")
    @NotBlank(message = "Refresh Token 값이 누락되었습니다.")
    private String refreshToken;
}
