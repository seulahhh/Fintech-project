package com.project.fintech.auth.otp;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpUtil {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final String issuer = "FintechService";
    /**
     * OTP secretKey 생성
     * @return 생성된 OTP secret key
     */
    public GoogleAuthenticatorKey createOtpSecretKey() {
        return gAuth.createCredentials();
    }

    /**
     * Provisioning URL 생성
     * @param email 사용자의 email
     * @return 생성된 ProvisiongUrl
     */
    public String createProvisioningUrl(String email, GoogleAuthenticatorKey secretKey) {
        log.info("생성된 비밀키: {}", secretKey.getKey());

        String otpProvisioningUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, email, secretKey);
        log.info("OTP Provisioning URI: {}", otpProvisioningUrl);
        return otpProvisioningUrl;
    }

    /**
     * secretKey로 OTP Code 유효성 검증
     * @param secretKey 매칭할 secretkey
     * @param code 발급된 OTP code
     * @return 유효성 검증 결과(true/false)
     */
    public boolean isCodeValid(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }
}
