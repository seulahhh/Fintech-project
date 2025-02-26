package com.project.fintech.application;

import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.auth.otp.OtpVerificationDto;
import com.project.fintech.client.EmailService;
import com.project.fintech.client.SendVerificationEmailForm;
import com.project.fintech.model.dto.EmailVerificationDto;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.service.AuthService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterApplication {
    private final OtpUtil otpUtil;
    private final EmailService emailService;
    private final AuthService authService;

    @Value("${brevo.link.base-url}")
    private String emailBaseUrl;

    @Value("${brevo.link.verify-url.email}")
    private String verifyEmailUrl;

    /**
     * 사용자가 회원가입을 요청 -> 가입 인증 메일 보내주기
     * @param registerRequestDto 회원가입 요청 dto (form)
     * @return 사용자에게 전송한 mail 본문(html)
     */
    @Transactional
    public String handleRegisterRequest(RegisterRequestDto registerRequestDto) {
        SendVerificationEmailForm sendVerificationEmailForm =
            emailService.toSendVerificationEmailForm(registerRequestDto, emailBaseUrl + verifyEmailUrl);
        authService.registerTemporaryUser(registerRequestDto);
        return emailService.sendSignupVerificationMail(sendVerificationEmailForm);
    }

    /**
     * 이메일계정 인증을 완료한 뒤 OTP 등록 URL을 반환한다.
     * @param EmailVerificationDto 가입요청한 사용자 email
     * @return 사용자가 OTP 인증 서비스등록을 할 수 있는 Provisioning URL
     */
    @Transactional
    public String completeEmailVerificationAndProvideOtpUrl(
        EmailVerificationDto EmailVerificationDto) {
        String userEmail = EmailVerificationDto.getEmail();
        authService.markEmailAsVerified(userEmail);
        GoogleAuthenticatorKey otpSecretKey = otpUtil.createOtpSecretKey();
        String provisioningUrl = otpUtil.createProvisioningUrl(userEmail, otpSecretKey);
        authService.saveOtpSecretKey(provisioningUrl, userEmail);
        return provisioningUrl;
    }

    /**
     * 발급한 secretKey로 OTP 인증을 마치면 OTP 등록여부를 완료처리한다.
     * @param otpVerificationDto
     */
    @Transactional
    public void completeOtpRegistration(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int otpCode = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.validateOtpCode(otpCode, email);
        // 인증 실패시 Exception을 던지고, 성공 시 이하 로직 수행
        authService.markOtpAsRegistered(email);
    }
}
