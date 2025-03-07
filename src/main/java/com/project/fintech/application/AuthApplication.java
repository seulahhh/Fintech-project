package com.project.fintech.application;

import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.auth.jwt.JwtUtil;
import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.model.dto.OtpVerificationDto;
import com.project.fintech.service.AuthService;
import com.project.fintech.model.dto.IssueTokenRequestDto;
import com.project.fintech.model.dto.LogoutRequestDto;
import com.project.fintech.model.dto.TokenPairDto;
import com.project.fintech.model.type.Message;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplication {

    private final OtpUtil otpUtil;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * OTP 재발급 시작하는 흐름
     *
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
     *
     * @param otpVerificationDto
     */
    public void completeOtpReissue(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int code = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.validateOtpCode(code, email);
        authService.markOtpAsRegistered(email);
    }

    /**
     * OTP인증이 필요한 서비스에서 OTP검증 수행 흐름
     *
     * @param otpVerificationDto
     */
    public void executeOtpVerification(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int code = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.validateOtpCode(code, email);
        // todo + 추후 redis에 OTP 인증 1회당 지속 유효시간과 함께 올리는 로직 추가
    }

    /**
     * 사용자가 로그인에 성공하면 Access Token과 Refresh Token을 발급해주는 흐름
     *
     * @param email
     * @return access token
     */
    public TokenPairDto issueTokenPair(String email) {
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        authService.storeRefreshToken(refreshToken, email);
        log.info("refreshToken = {}", refreshToken);
        log.info("authapplication내부: {}", SecurityContextHolder.getContext().getAuthentication());
        return TokenPairDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    /**
     * client측에서 refesh token으로 access token 재발급 요청 시 Refresh token을 검증 후 access token을 발급해주는 흐름
     *
     * @param issueTokenRequestDto refresh token, email
     * @return new access token
     */
    public ResponseDto<TokenPairDto> issueNewAccessTokenByRefreshToken(
        IssueTokenRequestDto issueTokenRequestDto) {
        String requestRefreshToken = issueTokenRequestDto.getRefreshToken();
        String email = issueTokenRequestDto.getEmail();
        authService.verifyRefreshTokenEmailPair(requestRefreshToken, email);
        jwtUtil.verifyToken(requestRefreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        authService.invalidateRefreshToken(requestRefreshToken);
        authService.storeRefreshToken(newRefreshToken, email);

        TokenPairDto tokenPairDto = TokenPairDto.builder().accessToken(newAccessToken)
            .refreshToken(newRefreshToken).build();

        return ResponseDto.<TokenPairDto>builder().code(HttpServletResponse.SC_OK)
                .message(Message.COMPLETE_ISSUE_TOKEN).data(tokenPairDto).build();
    }

    /**
     * Jwt token 인증이 필요한 경로에 대해 요청이 들어오면 token에 대한 인증을 진행하는 흐름 Filter에서 response, request 객체를 가지고
     * 진행한다.
     *
     * @param token
     */
    public void executeJwtAuthentication(String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        authService.verifyNotDisabledAccessToken(token, userEmail);
        jwtUtil.verifyToken(token);

        Authentication authentication = authService.getAuthenticationByToken(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * 로그아웃시 Jwt token에 대한 처리를 하는 흐름
     *
     * @param
     */
    public void processTokenWhenLogout(LogoutRequestDto logoutRequestDto) {
        authService.invalidateRefreshToken(logoutRequestDto.getRefreshToken());
        authService.addAccessTokenBlackList(logoutRequestDto.getAccessToken());
    }
}
