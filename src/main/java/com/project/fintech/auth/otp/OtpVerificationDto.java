package com.project.fintech.auth.otp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerificationDto {
    private String email;
    private int otpCode;
}
