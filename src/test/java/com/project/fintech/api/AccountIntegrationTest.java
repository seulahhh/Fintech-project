package com.project.fintech.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatStream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.builder.AccountTestDataBuilder;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.domain.AccountDto;
import com.project.fintech.model.type.Message;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.AccountRepository;
import com.project.fintech.persistence.repository.UserRepository;
import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class AccountIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AccountIntegrationTest.class);
    private final String testUserEmail = "testUser@test.com";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeAll
    public void setUp() throws Exception {
        User testUser = User.builder().name("name").phone("01010001000").email(testUserEmail)
            .isVerifiedEmail(true).isOtpRegistered(true)
            .password(passwordEncoder.encode("11111111")).build();
        OtpSecretKey otpSecretKey = OtpSecretKey.builder().user(testUser).secretKey("DDFJALK1")
            .build();
        testUser.setUserSecretKey(otpSecretKey);

        userRepository.save(testUser);

        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
            new UsernamePasswordAuthenticationToken(testUser.getEmail(), testUser.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(emptyContext);
    }


    @Test
    @DisplayName("계좌를 삭제하는 흐름 - 성공")
    void DeleteAccountTest_Success() throws Exception {
        //given
        User user = userRepository.findByEmail(testUserEmail)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Account account = new AccountTestDataBuilder().withUser(user).withBalance(0L).build();
//        Account save = accountRepository.save(account);

        user.getAccount().stream().forEach(x -> log.info("account의 id: {}", x.getId()));
        //when & then
        MvcResult mvcResult = mockMvc.perform(delete("/accounts/{accountId}",1))
            .andExpect(status().isOk()).andDo(print()).andReturn();
        ResponseDto<AccountDto> responseDto = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
            });
        assertThat(responseDto.getMessage()).isEqualTo(Message.COMPLETE_DELETE_ACCOUNT);
        assertThatStream(accountRepository.findActiveAccounts(user).stream()).noneMatch(
            a -> a.getId() == account.getId());
    }

    @Test
    @DisplayName("새로운 계좌를 생성하는 흐름 - 성공")
    void createAccountTest_Success() throws Exception {
        //given
        User user = userRepository.findByEmail(testUserEmail)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //when & then
        MvcResult mvcResult = mockMvc.perform(post("/accounts"))
            .andExpect(status().isOk()).andDo(print()).andReturn();

        ResponseDto<AccountDto> responseDto = objectMapper.readValue(
            mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
            });
        AccountDto accountDto = responseDto.getData();

        assertThatStream(accountRepository.findActiveAccounts(user).stream()).anyMatch(
            account -> account.getAccountNumber().equals(accountDto.getAccountNumber()));
        assertThatStream(user.getAccount().stream().map(Account::getId)).anyMatch(x -> x == accountDto.getId());
        log.info("user's accounts : {} ", user.getAccount().toString());
    }
}
