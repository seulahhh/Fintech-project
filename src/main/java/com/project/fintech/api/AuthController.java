package com.project.fintech.api;

import com.project.fintech.application.AuthApplication;
import com.project.fintech.model.dto.IssueTokenRequestDto;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.TokenPairDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplication authApplication;
    // ProvisioningUrl(=secretKey) 재발급
//    @PostMapping("/auth/otp/reset")
//    public ResponseEntity<?> resetSecretKey(@RequestParam String email) {
        // SecretKey를 재생성
        // Provisiong URL을 생성
        // 등록 가능한 경로 만들기
        // 등록 했다면 또 OTP 인증을 수행하기
//        return null;
//    } --> OTP 인증 및 재발급과 관련된 컨트롤러는 로그인, 로그아웃과 관련이 없어
//    잠시 주석 처리 해두었습니다. 해당 부분 구현 후 해제하겠습니다

    @PostMapping("/auth/jwt/issue")
    public ResponseEntity<ResponseDto<TokenPairDto>> issueTokens(
        @RequestBody IssueTokenRequestDto issueTokenRequestDto) {
        return ResponseEntity.ok(
            authApplication.issueNewAccessTokenByRefreshToken(issueTokenRequestDto));
    }
}
