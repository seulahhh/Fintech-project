package com.project.fintech.application;

import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.client.EmailService;
import com.project.fintech.client.SendVerificationEmailForm;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.UserEmailDto;
import com.project.fintech.model.type.Message;
import com.project.fintech.service.AuthService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletResponse;
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
     *
     * @param registerRequestDto 회원가입 요청 dto (form)
     * @return 사용자에게 전송한 mail 본문(html)
     */
    @Transactional
    public ResponseDto<String> handleRegisterRequest(RegisterRequestDto registerRequestDto) {
        SendVerificationEmailForm sendVerificationEmailForm = emailService.toSendVerificationEmailForm(
            registerRequestDto, emailBaseUrl + verifyEmailUrl);
        authService.registerTemporaryUser(registerRequestDto);
        return ResponseDto.<String>builder()
            .data(emailService.sendSignupVerificationMail(sendVerificationEmailForm))
            .code(HttpServletResponse.SC_OK).message(Message.COMPLETE_SEND_EMAIL).build();
    }

    /**
     * 이메일계정 인증을 완료한 뒤 OTP 등록 URL을 반환한다.
     *
     * @param UserEmailDto 가입요청한 사용자 email
     * @return 사용자가 OTP 인증 서비스등록을 할 수 있는 Provisioning URL
     */
    @Transactional
    public ResponseDto<String> completeEmailVerificationAndProvideOtpUrl(
        UserEmailDto UserEmailDto) {
        String userEmail = UserEmailDto.getEmail();
        authService.markEmailAsVerified(userEmail);
        GoogleAuthenticatorKey otpSecretKey = otpUtil.createOtpSecretKey();
        String provisioningUrl = otpUtil.createProvisioningUrl(userEmail, otpSecretKey);
        authService.saveOtpSecretKey(otpSecretKey.getKey(), userEmail);
        return ResponseDto.<String>builder().data(provisioningUrl).code(HttpServletResponse.SC_OK)
            .message(Message.COMPLETE_VERIFY_EMAIL).build();
    }
}
