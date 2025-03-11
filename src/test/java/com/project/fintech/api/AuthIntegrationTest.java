package com.project.fintech.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.auth.jwt.JwtUtil;
import com.project.fintech.auth.otp.OtpUtil;
import com.project.fintech.model.dto.IssueTokenRequestDto;
import com.project.fintech.model.dto.LoginRequestDto;
import com.project.fintech.model.dto.LogoutRequestDto;
import com.project.fintech.model.dto.OtpVerificationDto;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.TokenPairDto;
import com.project.fintech.model.dto.UserEmailDto;
import com.project.fintech.model.type.Message;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.OtpSecretKeyRepository;
import com.project.fintech.persistence.repository.UserRepository;
import com.project.fintech.service.AuthService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    public static final String DISABLED_TOKEN_PREFIX = "JWT_BLACKLIST::";
    public static final String REFRESH_TOKEN_PREFIX = "JWT_REFRESH_TOKEN::";
    private static final Logger log = LoggerFactory.getLogger(AuthIntegrationTest.class);
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpUtil otpUtil;

    private RedisServer redisServer;
    @Autowired
    private AuthService authService;
    @Autowired
    private OtpSecretKeyRepository otpSecretKeyRepository;


    @BeforeAll
    public void setUp() throws Exception {
        User testUser = User.builder().name("name").phone("01010001000")
            .email("prinarrow1219@gmail.com").isVerifiedEmail(true).isOtpRegistered(true)
            .password(passwordEncoder.encode("11111111")).build();
//        otpSecretKeyRepository.save();
//

        userRepository.save(testUser);
        redisServer = new RedisServer(6300);
        redisServer.start();
    }

    @AfterAll
    public void tearDown() throws IOException {
        if (redisServer != null) {
            Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection()
                .serverCommands().flushAll();
            stringRedisTemplate.delete("*");
            redisServer.stop();
        }
    }

    @Test
    @DisplayName("Login - 성공")
    @Transactional
    void loginTestWithGenerateTokens_Success() throws Exception {
        //given
        String userEmail = "prinarrow1219@gmail.com";
        String password = "11111111";
        LoginRequestDto loginRequestDto = LoginRequestDto.builder().email(userEmail)
            .password(password).build();

        String loginRequestDtoJson = objectMapper.writeValueAsString(loginRequestDto);

        //when & then
        mockMvc.perform(post("/auth/login").contentType("application/json;charset=UTF-8")
                .content(loginRequestDtoJson)).andExpect(jsonPath("$.message").value(
                Message.COMPLETE_ISSUE_TOKEN.getMessage())) // Jackson 직렬화 시 모종의 에러로 공백이 추가됨
            .andExpect(jsonPath("$.code").value(HttpServletResponse.SC_OK))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("Login - 실패  - 이메일에 해당하는 회원이 존재 하지 않을 때")
    void loginTestWithGenerateTokens_Fail_WhenNotFoundUser() throws Exception {
        //given
        String userEmail = "unableUser@test.com";
        String password = "11111111";
        LoginRequestDto loginRequestDto = LoginRequestDto.builder().email(userEmail)
            .password(password).build();

        String loginRequestDtoJson = objectMapper.writeValueAsString(loginRequestDto);
        //when & then
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(loginRequestDtoJson)).andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andDo(print());
    }

    @Test
    @DisplayName("Access token 재발급 - 성공")
    void issueNewAccessTokenByRefreshTokenTest_Success() throws Exception {
        //given
        String userEmail = "prinarrow1219@gmail.com";
        String refreshToken = jwtUtil.generateRefreshToken(userEmail);
        stringRedisTemplate.opsForValue().set("JWT_REFRESH_TOKEN::" + refreshToken, userEmail);

        IssueTokenRequestDto issueTokenRequestDto = IssueTokenRequestDto.builder()
            .refreshToken(refreshToken).email(userEmail).build();
        String issueTokenRequestDtoJson = objectMapper.writeValueAsString(issueTokenRequestDto);

        //when & then
        MvcResult mvcResult = mockMvc.perform(
                post("/auth/jwt/issue").contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(issueTokenRequestDtoJson)).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists()).andDo(print()).andReturn();
        ResponseDto<TokenPairDto> tokenPairDtoResponseDto = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        String newRefreshToken = tokenPairDtoResponseDto.getData().getRefreshToken();
        String result = stringRedisTemplate.opsForValue()
            .get(REFRESH_TOKEN_PREFIX + newRefreshToken);
        assertThat(result).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Logout시 JWT token 처리 - 성공")
    void logoutTestWithInvalidatesTokens_Success() throws Exception {
        String userEmail = "prinarrow1219@gmail.com";
        String refreshToken = jwtUtil.generateRefreshToken(userEmail);
        String accessToken = jwtUtil.generateAccessToken(userEmail);
        stringRedisTemplate.opsForValue().set("JWT_REFRESH_TOKEN::" + refreshToken, userEmail);
        LogoutRequestDto logoutRequestDto = LogoutRequestDto.builder().accessToken(accessToken)
            .refreshToken(refreshToken).build();
        String logoutRequestDtoJson = objectMapper.writeValueAsString(logoutRequestDto);

        //when & then
        mockMvc.perform(post("/auth/logout").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(logoutRequestDtoJson)).andDo(print());

        String redisRefreshToken = stringRedisTemplate.opsForValue()
            .get(REFRESH_TOKEN_PREFIX + logoutRequestDto.getRefreshToken());
        assertThat(redisRefreshToken).isNullOrEmpty();
        String redisAccessToken = stringRedisTemplate.opsForValue()
            .get(DISABLED_TOKEN_PREFIX + logoutRequestDto.getAccessToken());
        assertThat(redisAccessToken).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("OTP 인증이 필요한 URL에 대한 OTP code 검증하는 흐름")
    @WithMockUser(username = "testUser@test.com", roles = {"USER"})
    @Transactional
    void verifyOtpCodeTest_Success() throws Exception {
        //given
        GoogleAuthenticator gAuth = new GoogleAuthenticator();

        String userEmail = "testUser@test.com";
        String password = "11111111";
        User user = User.builder().name("name").phone("01010001000").email(userEmail)
            .isVerifiedEmail(true).isOtpRegistered(true)
            .password(passwordEncoder.encode(password)).build();
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey(otpUtil.createOtpSecretKey().getKey()).user(user).build();
        user.setUserSecretKey(otpSecretKey);
        userRepository.save(user);

        int userOtpCode = gAuth.getTotpPassword(user.getOtpSecretKey().getSecretKey(), System.currentTimeMillis());
        OtpVerificationDto otpVerificationDto = OtpVerificationDto.builder()
            .otpCode(userOtpCode).email(userEmail).build();
        String otpVerificationDtoJson = objectMapper.writeValueAsString(otpVerificationDto);

        //when & then
        mockMvc.perform(post("/auth/otp/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(otpVerificationDtoJson))
            .andExpect(status().is(HttpServletResponse.SC_OK))
            .andExpect(jsonPath("$.message").value(Message.COMPLETE_VERIFY_OTP.getMessage()))
            .andDo(print());
    }

    @Test
    @DisplayName("OTP secret key를 재발급 하는 흐름")
    @Transactional
    @WithMockUser(username = "testUser@test.com", roles = {"USER"})
    void reIssueOtpSecretKeyAndInvalidateOlderKeyTest_Success() throws Exception {
        //given
        String userEmail = "testUser@test.com";
        String password = "11111111";
        User user = User.builder().name("name").phone("01010001000").email(userEmail)
            .isVerifiedEmail(true).isOtpRegistered(true)
            .password(passwordEncoder.encode(password)).build();
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().secretKey(otpUtil.createOtpSecretKey().getKey()).user(user).build();
        user.setUserSecretKey(otpSecretKey);
        userRepository.save(user);

        UserEmailDto userEmailDto = UserEmailDto.builder().email(userEmail).build();
        String userEmailDtoJson = objectMapper.writeValueAsString(userEmailDto);

        //when & then
        mockMvc.perform(
                post("/auth/otp/reset").contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(userEmailDtoJson))
            .andExpect(status().is(HttpServletResponse.SC_OK));

        assertThat(user.getIsOtpRegistered()).isFalse();

        String secretKeyBeforeReset = otpSecretKey.getSecretKey();
        String secretKeyAfterReset = user.getOtpSecretKey().getSecretKey();
        assertThat(secretKeyBeforeReset).isNotEqualTo(secretKeyAfterReset);
    }
}
