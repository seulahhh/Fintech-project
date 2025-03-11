package com.project.fintech.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerificationDto {
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수 입니다.")
    private String email;

    @Min(0)
    @Max(999999)
    @NotNull(message = "OTP 코드는 필수 입력 사항입니다.")
    private Integer otpCode;
}
