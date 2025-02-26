package com.project.fintech.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailVerificationDto {
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수 입니다.")
    private String email;
}