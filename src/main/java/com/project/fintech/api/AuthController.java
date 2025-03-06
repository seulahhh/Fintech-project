package com.project.fintech.api;

import com.project.fintech.application.AuthApplication;
import com.project.fintech.model.dto.IssueTokenRequestDto;
import com.project.fintech.model.dto.OtpVerificationDto;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.TokenPairDto;
import com.project.fintech.model.dto.UserEmailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplication authApplication;

    /**
     * OTP secret key를 재발급하고 Provision URL 반환
     * @param userEmailDto
     * @return
     */
    @PostMapping("/auth/otp/reset")
    public ResponseEntity<ResponseDto<String>> resetSecretKey(@RequestBody UserEmailDto userEmailDto) {
        return ResponseEntity.ok(authApplication.issueNewOtpSecretAndSendUrl(userEmailDto));
    }

    /**
     * Access Token과 Refresh Token을 발급
     *
     * @param issueTokenRequestDto
     * @return
     */
    @PostMapping("/auth/jwt/issue")
    public ResponseEntity<ResponseDto<TokenPairDto>> issueTokens(
        @RequestBody IssueTokenRequestDto issueTokenRequestDto) {
        return ResponseEntity.ok(
            authApplication.issueNewAccessTokenByRefreshToken(issueTokenRequestDto));
    }

    /**
     * OTP 코드를 검증
     * @param otpVerificationDto
     * @param session
     * @return
     */
    @PostMapping("/auth/otp/verify")
    public ResponseEntity<ResponseDto<String>> verifyOtpCode(@RequestBody OtpVerificationDto otpVerificationDto) {
        return ResponseEntity.ok(authApplication.executeOtpVerification(otpVerificationDto));
    }
}
