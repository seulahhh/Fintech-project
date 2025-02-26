package com.project.fintech.service;

import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.OtpSecretKeyRepository;
import com.project.fintech.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpUtil otpUtil;
    private final OtpSecretKeyRepository otpSecretKeyRepository;

    /**
     * 이메일 중복 여부 체크
     *
     * @param email 사용자 이메일
     * @return 중복 여부
     */
    public void isNotDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_EMAIL);
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
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        user.setVerifiedEmail(true);
    }

    /**
     * OTP SecretKey를 DB에 저장
     * @param secretKey 생성한 secretKey
     * @param email secretkey의 주인이 되는 user의 email
     */
    @Transactional
    public void saveOtpSecretKey(String secretKey, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey(secretKey).user(user).build();
        otpSecretKeyRepository.save(otpSecretKey);
    }

    /**
     * OTP 등록 여부를 전환 (isRegistredOtp = true)
     * @param email
     */
    @Transactional
    public void markOtpAsRegistered(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        user.setOtpRegistered(true);
    }

    /**
     * DB에서 사용자의 email로 OTP secret key 조회
     * @param email
     * @return 조회한 OTP secret key
     */
    @Transactional
    public String getUserSecretKey(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        OtpSecretKey otpSecretKey = otpSecretKeyRepository.findByUser(user)
            .orElseThrow(() -> new CustomException(ErrorCode.OTP_NOT_REGISTERED));
        return otpSecretKey.getSecretKey();
    }

    /**
     * 사용자가 Google Authenticator를 통해 발급받아 서버로 입력한 OTP를 검증하기
     * @param code 사용자가 입력한 OTP code
     * @param email
     */
    @Transactional
     public void validateOtpCode(int code, String email) {
        String userSecretKey = getUserSecretKey(email);
        boolean codeValid = otpUtil.isCodeValid(userSecretKey, code);
        if (!codeValid) {
            throw new CustomException(ErrorCode.NOT_VALID_OTP_CODE);
        }
    }
}