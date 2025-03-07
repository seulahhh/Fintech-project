package com.project.fintech.service;

import com.project.fintech.auth.CustomUserDetailsService;
import com.project.fintech.auth.jwt.JwtUtil;
import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.OtpSecretKeyRepository;
import com.project.fintech.persistence.repository.UserRepository;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String DISABLED_TOKEN_PREFIX = "JWT_BLACKLIST::";
    public static final String REFRESH_TOKEN_PREFIX = "JWT_REFRESH_TOKEN::";
    public static final String OTP_COUNTING_PREFIX = "OTP_COUNTING::";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpUtil otpUtil;
    private final JwtUtil jwtUtil;
    private final OtpSecretKeyRepository otpSecretKeyRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 이메일 중복 여부 체크
     *
     * @param email 사용자 이메일
     * @return 중복 여부
     */
    public void isNotDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
    }

    /**
     * 사전 회원가입(이메일 인증 및 OTP 등록 전)
     *
     * @param registerRequestDto 회원가입 폼
     */
    @Transactional
    public void registerTemporaryUser(RegisterRequestDto registerRequestDto) {
        isNotDuplicateEmail(registerRequestDto.getEmail());

        User user = User.builder().name(registerRequestDto.getName())
            .email(registerRequestDto.getEmail()).phone(registerRequestDto.getPhone())
            .password(passwordEncoder.encode(registerRequestDto.getPassword())).build();

        userRepository.save(user);
    }

    /**
     * 이메일 인증 여부를 전환(isVerifiedEmail = true)
     *
     * @param email
     */
    @Transactional
    public void markEmailAsVerified(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setVerifiedEmail(true);
    }

    /**
     * OTP SecretKey를 DB에 저장
     *
     * @param secretKey 생성한 secretKey
     * @param email     secretkey의 주인이 되는 user의 email
     */
    @Transactional
    public void saveOtpSecretKey(String secretKey, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey(secretKey).user(user).build();
        otpSecretKeyRepository.save(otpSecretKey);
    }

    /**
     * DB에 저장된 사용자의 OTP secret key 삭제 (OTP 재등록 시, disabled 시)
     *
     * @param email userEmail
     */
    @Transactional
    public void invalidateOtpSecretKey(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        OtpSecretKey otpSecretKey = otpSecretKeyRepository.findByUser(user)
            .orElseThrow(() -> new CustomException(ErrorCode.OTP_SECRET_KEY_NOT_FOUND));
        user.setOtpSecretKeyNull();
        userRepository.save(user);
    }

    /**
     * OTP 등록 여부를 전환
     *
     * @param email
     */
    @Transactional
    public void markOtpAsRegistered(String email, Boolean bool) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setOtpRegistered(bool);
    }

    /**
     * DB에서 사용자의 email로 OTP secret key 조회
     *
     * @param email
     * @return 조회한 OTP secret key
     */
    @Transactional
    public String getUserSecretKey(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        OtpSecretKey otpSecretKey = otpSecretKeyRepository.findByUser(user)
            .orElseThrow(() -> new CustomException(ErrorCode.OTP_SECRET_KEY_NOT_FOUND));
        return otpSecretKey.getSecretKey();
    }

    /**
     * 사용자가 Google Authenticator를 통해 발급받아 서버로 입력한 OTP를 검증하기
     *
     * @param code  사용자가 입력한 OTP code
     * @param email
     */
    @Transactional
    public void verifyOtpCode(int code, String email) {
        String attemptedCount = stringRedisTemplate.opsForValue().get(OTP_COUNTING_PREFIX + email);
        if (attemptedCount != null && Integer.parseInt(attemptedCount) >= 3) {
            throw new CustomException(ErrorCode.OTP_ATTEMPT_EXCEEDED);
        }

        String userSecretKey = getUserSecretKey(email);
        boolean codeValid = otpUtil.isCodeValid(userSecretKey, code);
        if (!codeValid) {
            countOtpAttempt(email);
            throw new CustomException(ErrorCode.INVALID_OTP_CODE);
        }
    }


    /**
     * OTP 인증 시도 횟수 session(redis)에 카운팅해서 저장
     *
     * @param email
     */
    public void countOtpAttempt(String email) {
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        int period = 30;
        long secondsPassed = currentTimeInSeconds % period;
        long secondsRemaining = period - secondsPassed;

        Long newCount = stringRedisTemplate.opsForValue().increment(OTP_COUNTING_PREFIX + email);
        if (newCount == 1) {
            stringRedisTemplate.expire(OTP_COUNTING_PREFIX + email, secondsRemaining,
                TimeUnit.HOURS);
        }
    }

    /**
     * 인증 시도 실패 횟수 삭제
     *
     * @param email
     */
    public void deleteOtpAttempt(String email) {
        stringRedisTemplate.delete(OTP_COUNTING_PREFIX + email);
    }

    /**
     * token정보로 Authentication 객체 생성
     *
     * @param token
     * @return Authentication 객체
     */
    public Authentication getAuthenticationByToken(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
    }

    /**
     * Redis에서 Refresh Token 삭제 (로그아웃 혹은 access 토큰 재발급 시)
     *
     * @param refreshToken
     */
    public void invalidateRefreshToken(String refreshToken) {
        if (!stringRedisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        }
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
    }

    /**
     * Redis에 Refresh Token 저장 key - JWT_REFRESH_TOKEN::{refresh token}// value - user email
     *
     * @param token refresh Token
     */
    public void storeRefreshToken(String token, String email) {
        stringRedisTemplate.opsForValue()
            .set(REFRESH_TOKEN_PREFIX + token, email, 7, TimeUnit.DAYS);
    }


    /**
     * Redis에 Access Token을 black list에 저장
     *
     * @param token
     */
    public void addAccessTokenBlackList(String token) {
        Date expiration = jwtUtil.getTokenExpiration(token);
        String email = jwtUtil.getEmailFromToken(token);
        Date current = new Date(System.currentTimeMillis());

        if (current.before(expiration)) {
            long diffInMillies = expiration.getTime() - current.getTime();
            stringRedisTemplate.opsForValue()
                .set(DISABLED_TOKEN_PREFIX + token, email, diffInMillies, TimeUnit.SECONDS);
        }
    }

    /**
     * Redis에서 Refresh token 키의 value와 user를 비교하여 저장된 refresh token의 유효성 확인
     *
     * @param token refresh token
     * @param email user email
     */
    public void verifyRefreshTokenEmailPair(String token, String email) {
        String userEmail = stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + token);
        if (userEmail == null) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        } else if (!userEmail.equals(email)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_USER_MISMATCH);
        }
    }

    /**
     * Redis에 사용자의 access token이 black list로 저장되어 있지 않은지 확인
     *
     * @param token
     * @param email
     */
    public void verifyNotDisabledAccessToken(String token, String email) {
        String userEmail = stringRedisTemplate.opsForValue().get(DISABLED_TOKEN_PREFIX + token);
        if (stringRedisTemplate.hasKey(DISABLED_TOKEN_PREFIX + token) && userEmail.equals(email)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}