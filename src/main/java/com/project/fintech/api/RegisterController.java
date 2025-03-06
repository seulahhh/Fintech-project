package com.project.fintech.api;

import com.project.fintech.application.AuthApplication;
import com.project.fintech.application.RegisterApplication;
import com.project.fintech.model.dto.OtpVerificationDto;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.model.dto.UserEmailDto;
import com.project.fintech.model.type.Message;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterApplication registerApplication;
    private final AuthApplication authApplication;

    /**
     * 회원가입 요청을 받으면 인증 메일 발송하기
     *
     * @param registerRequestDto
     * @return 이메일 인증 URL을 포함한 인증메일 body
     */
    @PostMapping("/users")
    public ResponseEntity<String> sendRegisterEmail(
        @Valid @RequestBody RegisterRequestDto registerRequestDto) {

        return ResponseEntity.ok(registerApplication.handleRegisterRequest(registerRequestDto));
    }

    /**
     * 이메일 인증 완료 후 OTP 등록 URI 반환
     *
     * @param email
     * @return OTP 등록 가능한 provisioning uri
     */
    @PostMapping("/auth/email/verify")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody UserEmailDto UserEmailDto) {

        return ResponseEntity.ok(registerApplication.completeEmailVerificationAndProvideOtpUrl(
            UserEmailDto));
    }

    /**
     * OTP SecretKey 등록하기
     *
     * @param otpVerificationDto OTP code를 담은 request dto
     * @return 등록 완료 message
     */
    @PostMapping("/auth/otp/register")
    public ResponseEntity<Message> registerOtp(@Valid @RequestBody OtpVerificationDto otpVerificationDto) {
        authApplication.completeOtpRegistration(otpVerificationDto);

        return ResponseEntity.ok(Message.COMPLETE_REGISTERED_OTP);
    }
}
