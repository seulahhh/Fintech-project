package com.project.fintech.auth.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerificationDto {
    private String email;

    @NotBlank(message = "OTP 코드는 필수 입력 사항입니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP 코드는 6자리 숫자로 입력해 주세요.")
    private String otpCode;
}
