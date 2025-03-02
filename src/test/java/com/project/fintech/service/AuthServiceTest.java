package com.project.fintech.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.builder.RegisterRequestDtoTestDataBuilder;
import com.project.fintech.builder.UserTestDataBuilder;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.OtpSecretKeyRepository;
import com.project.fintech.persistence.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    OtpSecretKeyRepository otpSecretKeyRepository;

    @Mock
    OtpUtil otpUtil;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("이메일 중복 여부 체크 - 성공")
    void isNotDuplicateEmail_Success() {
        //given
        String email = "test@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        //when & then
        assertThatCode(() -> authService.isNotDuplicateEmail(email)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이메일 중복 여부 체크 - 실패(중복된 이메일이 존재)")
    void isNotDuplicateEmail_Fail_WhenExistDuplicateEmail() {
        //given
        User user = new UserTestDataBuilder().build();
        String email = user.getEmail();
        when(userRepository.existsByEmail(email)).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.isNotDuplicateEmail(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ALREADY_EXIST_EMAIL);
    }

    @Test
    @DisplayName("가입 인증 전 사전 회원 등록 - 성공")
    void registerTemporaryUser_Success() {
        //given
        String email = "test@test.com";
        RegisterRequestDto registerRequestDto = new RegisterRequestDtoTestDataBuilder().withEmail(
            email).build();
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(registerRequestDto.getPassword())).thenReturn(encodedPassword);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(
            invocation -> invocation.getArgument(0));

        //when
        authService.registerTemporaryUser(registerRequestDto);

        //then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(registerRequestDto.getEmail());
        assertThat(savedUser.getPhone()).isEqualTo(registerRequestDto.getPhone());
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getName()).isEqualTo(registerRequestDto.getName());
    }

    @Test
    @DisplayName("가입 인증 전 사전 회원 등록 - 실패(중복된 이메일이 존재할 때)")
    void registerTemporaryUser_Fail_WhenExistDuplicateEmail() {
        //given
        User user = new UserTestDataBuilder().build();
        String email = user.getEmail();
        RegisterRequestDto registerRequestDto = new RegisterRequestDtoTestDataBuilder().withEmail(
            email).build();
        when(userRepository.existsByEmail(email)).thenReturn(true);

        //when&then
        assertThatThrownBy(
            () -> authService.registerTemporaryUser(registerRequestDto)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ALREADY_EXIST_EMAIL);
    }

    @Test
    @DisplayName("이메일 인증 여부 전환 - 성공")
    void markEmailAsVerified_Success() {
        //given
        User user = new UserTestDataBuilder().build();
        String email = user.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //when
        authService.markEmailAsVerified(email);

        //then
        assertThat(user.getIsVerifiedEmail()).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 여부 전환 - 실패")
    void markEmailAsVerified_Fail_WhenUserNotFound() {
        //given
        String email = "zerobase@zero.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authService.markEmailAsVerified(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_USER);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("OTP SecretKey DB에 저장 - 성공")
    void saveOtpSecretKey_Success() {
        //given
        String email = "zerobase@zero.com";
        String secretKey = "12341234";
        ArgumentCaptor<OtpSecretKey> captor = ArgumentCaptor.forClass(OtpSecretKey.class);
        User user = new UserTestDataBuilder().withEmail(email).build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //when
        authService.saveOtpSecretKey(secretKey, email);
        verify(otpSecretKeyRepository, times(1)).save(captor.capture()); // capture를 진행
        OtpSecretKey value = captor.getValue();

        //then
        assertThat(value.getSecretKey()).isEqualTo(secretKey);
    }

    @Test
    @DisplayName("OTP SecretKey DB에 저장 - 실패 (user 검색 실패)")
    void saveOtpSecretKey_Fail_WhenUserNotFound() {
        //given
        String email = "zerobase@zero.com";
        String secretKey = "12341234";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authService.saveOtpSecretKey(secretKey, email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_USER);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("OTP 등록 여부 전환 - 성공")
    void markOtpAsRegistered_Success() {
        //given
        User user = new UserTestDataBuilder().build();
        String email = user.getEmail();

        //when
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        authService.markOtpAsRegistered(email);

        //then
        assertThat(user.getIsOtpRegistered()).isEqualTo(true);
    }

    @Test
    @DisplayName("OTP 등록 여부 전환 - 실패(사용자를 찾을 수 없을때)")
    void markOtpAsRegistered_Fail_WhenNotFoundUser() {
        //given
        User user = new UserTestDataBuilder().build();
        String email = user.getEmail();

        //when
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> authService.markOtpAsRegistered(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("DB에서 사용자의 email로 OTP secret key 조회 - 성공")
    void getUserSecretKey_Success() {
        //given
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey("TESTKEY").build();
        User user = new UserTestDataBuilder().withOtpSecretKey(otpSecretKey).build();
        String email = user.getEmail();

        //when
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpSecretKeyRepository.findByUser(user)).thenReturn(Optional.of(otpSecretKey));
        String userSecretKey = authService.getUserSecretKey(email);

        //then
        assertThat(userSecretKey).isEqualTo(otpSecretKey.getSecretKey());
    }

    @Test
    @DisplayName("DB에서 사용자의 email로 OTP secret key 조회 - 실패(사용자를 찾을 수 없을때)")
    void getUserSecretKey_Fail_WhenNotFoundUser() {
        //given
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey("TESTKEY").build();
        User user = new UserTestDataBuilder().withOtpSecretKey(otpSecretKey).build();
        String email = user.getEmail();

        //when
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> authService.getUserSecretKey(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND_USER);
        verify(userRepository, times(1)).findByEmail(email);

    }

    @Test
    @DisplayName("DB에서 사용자의 email로 OTP secret key 조회 - 실패(사용자의 OTP secretkey가 없을 때(등록전))")
    void getUserSecretKey_Fail_WhenOtpNotRegistered() {
        //given
        User user = new UserTestDataBuilder().withOtpSecretKey(null).build();
        String email = user.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpSecretKeyRepository.findByUser(user)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authService.getUserSecretKey(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.OTP_NOT_REGISTERED);
        verify(otpSecretKeyRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("사용자가 입력한 OTP 코드 검증 - 성공")
    void validateOtpCode_Success() {
        //given
        int code = 301304;
        User user = new UserTestDataBuilder().build();
        OtpSecretKey otpSecretKey = user.getOtpSecretKey();
        String email = user.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpSecretKeyRepository.findByUser(user)).thenReturn(Optional.of(otpSecretKey));
        when(otpUtil.isCodeValid(otpSecretKey.getSecretKey(), code)).thenReturn(true);

        //when & then
        assertThatCode(() -> authService.validateOtpCode(code, email)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자가 입력한 OTP 코드 검증 - 실패(OTP 코드 검증에 실패했을 때)")
    void validateOtpCode_Fail_WhenNotValidCode() {
        //given
        int code = 301304;
        User user = new UserTestDataBuilder().build();
        OtpSecretKey otpSecretKey = user.getOtpSecretKey();
        String email = user.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpSecretKeyRepository.findByUser(user)).thenReturn(Optional.of(otpSecretKey));
        when(otpUtil.isCodeValid(otpSecretKey.getSecretKey(), code)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.validateOtpCode(code, email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.NOT_VALID_OTP_CODE);
    }
}
