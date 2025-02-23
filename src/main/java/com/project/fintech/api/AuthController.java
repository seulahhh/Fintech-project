package com.project.fintech.api;

import com.project.fintech.application.AuthApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthApplication authApplication;

    // ProvisioningUrl(=secretKey) 재발급
    @PostMapping("/auth/otp/reset")
    public ResponseEntity<?> resetSecretKey(@RequestParam String email) {
        // SecretKey를 재생성
        // Provisiong URL을 생성
        // 등록 가능한 경로 만들기
        // 등록 했다면 또 OTP 인증을 수행하기
        return null;
    }

}
