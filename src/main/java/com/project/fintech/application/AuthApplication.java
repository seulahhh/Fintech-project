package com.project.fintech.application;

import com.project.fintech.auth.jwt.JwtUtil;
import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.model.dto.IssueTokenRequestDto;
import com.project.fintech.model.dto.LogoutRequestDto;
import com.project.fintech.model.dto.OtpVerificationDto;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.TokenPairDto;
import com.project.fintech.model.dto.UserEmailDto;
import com.project.fintech.model.type.Message;
import com.project.fintech.service.AuthService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplication {

    private final OtpUtil otpUtil;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * OTP인증이 필요한 서비스에서 OTP검증 수행 흐름
     *
     * @param otpVerificationDto
     */
    public ResponseDto<String> executeOtpVerification(OtpVerificationDto otpVerificationDto,
        HttpSession session) {
        String email = otpVerificationDto.getEmail();
        int code = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.verifyOtpCode(code, email);
        authService.resetOtpAttemptCounts(email);
        session.setAttribute("OTPVerified", true);
        return ResponseDto.<String>builder().data("").message(Message.COMPLETE_VERIFY_OTP)
            .code(HttpServletResponse.SC_OK).build();
    }

    /**
     * OTP secret key 재발급 및 provisioning url 생성 시작 재발급 이후 /auth/otp/register 에서 OTP 등록을 해야 합니다
     *
     * @param userEmailDto
     * @return provisioning URL
     */
    @Transactional
    public ResponseDto<String> issueNewOtpSecretAndSendUrl(UserEmailDto userEmailDto) {
        String userEmail = userEmailDto.getEmail();

        authService.invalidateOtpSecretKey(userEmail);
        authService.markOtpAsRegistered(userEmail, false);
        GoogleAuthenticatorKey otpSecretKey = otpUtil.createOtpSecretKey();

        String provisioningUrl = otpUtil.createProvisioningUrl(userEmail, otpSecretKey);
        log.info("{}", otpSecretKey);
        authService.saveOtpSecretKey(otpSecretKey.getKey(), userEmail);
        return ResponseDto.<String>builder().data(provisioningUrl).code(HttpServletResponse.SC_OK)
            .message(Message.COMPLETE_ISSUE_SECRETKEY).build();
    }

    /**
     * 발급한 secretKey로 OTP 인증을 마치면 OTP 등록여부를 완료처리한다.
     *
     * @param otpVerificationDto
     */
    @Transactional
    public ResponseDto<String> completeOtpRegistration(OtpVerificationDto otpVerificationDto) {
        String email = otpVerificationDto.getEmail();
        int otpCode = Integer.parseInt(otpVerificationDto.getOtpCode());
        authService.verifyOtpCode(otpCode, email);
        authService.resetOtpAttemptCounts(email);
        authService.markOtpAsRegistered(email, true);
        return ResponseDto.<String>builder().data(null).message(Message.COMPLETE_REGISTERED_OTP)
            .code(HttpServletResponse.SC_OK).build();
    }

    /**
     * 사용자가 로그인에 성공하면 Access Token과 Refresh Token을 발급해주는 흐름
     *
     * @param email
     * @return access token
     */
    @Transactional
    public TokenPairDto issueTokenPair(String email) {
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        authService.storeRefreshToken(refreshToken, email);
        log.info("refreshToken = {}", refreshToken);
        return TokenPairDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    /**
     * client측에서 refesh token으로 access token 재발급 요청 시 Refresh token을 검증 후 access token을 발급해주는 흐름
     *
     * @param issueTokenRequestDto refresh token, email
     * @return new access token
     */
    @Transactional
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
    @Transactional
    public void executeJwtAuthentication(String token) {
        String userEmail = jwtUtil.getEmailFromToken(token);
        authService.verifyNotDisabledAccessToken(token, userEmail);
        jwtUtil.verifyToken(token);

        Authentication authentication = authService.getAuthenticationByToken(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 로그아웃시 Jwt token과 session에 대한 처리를 하는 흐름
     *
     * @param
     */
    @Transactional
    public void processTokenWhenLogout(LogoutRequestDto logoutRequestDto) {
        authService.invalidateRefreshToken(logoutRequestDto.getRefreshToken());
        authService.addAccessTokenBlackList(logoutRequestDto.getAccessToken());
    }
}
