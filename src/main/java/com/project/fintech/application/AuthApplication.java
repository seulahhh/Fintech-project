package com.project.fintech.application;

import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.auth.otp.OtpVerificationDto;
import com.project.fintech.service.AuthService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplication {
    private final OtpUtil otpUtil;
    private final AuthService authService;

    /**
     * OTP 재발급 시작하는 흐름
     * @param email
     */
    public String issueNewOtpSecretAndSendUrl(String email) {
        GoogleAuthenticatorKey otpSecretKey = otpUtil.createOtpSecretKey();
        String provisioningUrl = otpUtil.createProvisioningUrl(email, otpSecretKey);
        authService.saveOtpSecretKey(otpSecretKey.getKey(), email);
        return provisioningUrl;
    }

    /**
     * OTP 재발급 완료하는 흐름
     * @param otpVerificationDto
     */
    public void completeOtpReissue(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int code = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.validateOtpCode(code , email);
        authService.markOtpAsRegistered(email);
    }

    /**
     * OTP인증이 필요한 서비스에서 OTP검증 수행 흐름
     * @param otpVerificationDto
     */
    public void executeOtpVerification(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int code = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.validateOtpCode(code, email);
        // todo + 추후 redis에 OTP 인증 1회당 지속 유효시간과 함께 올리는 로직 추가
    }
}
