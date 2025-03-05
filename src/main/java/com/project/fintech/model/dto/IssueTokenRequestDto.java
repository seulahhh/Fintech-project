package com.project.fintech.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "IssueTokenRequestDto", description = "Access 토큰 재생성 요청 시 사용하는 DTO")
public class IssueTokenRequestDto {
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수 입니다.")
    private String email;

    @NotBlank(message = "Refresh Token 값이 누락되었습니다.")
    private String refreshToken;
}