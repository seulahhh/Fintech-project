package com.project.fintech.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.builder.RegisterRequestDtoTestDataBuilder;
import com.project.fintech.client.EmailService;
import com.project.fintech.model.dto.RegisterRequestDto;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.UserRepository;
import java.io.IOException;
import java.util.Optional;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegisterIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private MockWebServer mockWebServer;

    @BeforeEach //MockWebServer 시작
    void setUp() throws IOException {
        // ? 요청을 외부 API가 아닌 MockWebServer로 보내게 설정
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        ReflectionTestUtils.setField(emailService, "brevoWebClient",
            WebClient.builder().baseUrl(mockWebServer.url("").toString()).build());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("사용자에게 가입 요청을 받으면 사용자로부터 이메일 인증과 OTP 인증을 요구하는 흐름")
    void handleRegisterRequestFlowTest_Success() throws Exception {
        String email = "test_mail00@test.com";

        RegisterRequestDto registerRequestDto = new RegisterRequestDtoTestDataBuilder().withEmail(email).build();

        //given
        String requestBody = objectMapper.writeValueAsString(registerRequestDto);
        mockWebServer.enqueue(new MockResponse()
            .setBody("{\"status\": \"success\"}")
            .addHeader("Content-Type", "application/json")
            .setResponseCode(200));

        //when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(content().string(containsString("Fintech Verification Email For Sign Up")))
            .andDo(print());

        //then
        Optional<User> byEmail = userRepository.findByEmail(email);
        assertThat(byEmail).isPresent();
    }
}
