package com.project.fintech.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.fintech.builder.AccountTestDataBuilder;
import com.project.fintech.builder.TransactionTestDataBuilder;
import com.project.fintech.builder.UserTestDataBuilder;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.domain.TransactionMapper;
import com.project.fintech.model.type.Status;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.AccountRepository;
import com.project.fintech.persistence.repository.ArchivedTransactionRepository;
import com.project.fintech.persistence.repository.TransactionRepository;
import com.project.fintech.persistence.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceTest.class);
    @Mock
    AccountRepository accountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    ArchivedTransactionRepository archivedTransactionRepository;

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AccountService accountService;

    private Long MAX_ACCOUNT_COUNT = 3L;
    private String SERVICE_CODE = "177";

    @Test
    @DisplayName("Email로 User 가져오기 - 성공")
    void getUserByEmail_Success() {
        //given
        String email = "test@test.com";
        User user = new UserTestDataBuilder().withEmail(email).build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        //when
        accountService.getUserByEmail(email);

        //then
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository, times(1)).findByEmail(emailCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo(email);
    }

    @Test
    @DisplayName("Email로 User 가져오기 - 실패 - email에 해당하는 사용자가 DB에 없을 때")
    void getUserByEmail_Fail_WhenUserNotFound() {
        //given
        String email = "test@test.com";
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> accountService.getUserByEmail(email)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("accountId로 Account 가져오기 - 성공")
    void getAccountById_Success() {
        //given
        Account account = new AccountTestDataBuilder().build();
        Long accountId = account.getId();
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        //when
        accountService.getAccountById(accountId);

        //then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(accountRepository, times(1)).findById(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(accountId);
    }

    @Test
    @DisplayName("accountId로 Account 가져오기 - 실패")
    void getAccountById_Fail_WhenAccountNotFound() {
        //given
        Long accountId = 1L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> accountService.getAccountById(accountId)).isInstanceOf(
            CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("계좌 생성 - 성공")
    void createAccount_Success() {
        //given
        Account account = new AccountTestDataBuilder().build();
        User user = new UserTestDataBuilder().withAccounts(new ArrayList<>(List.of(account)))
            .build();
        when(accountRepository.countActiveAccountsByUser(any())).thenReturn(1L);

        //when
        Account newAccount = accountService.createAccount(user);

        //then
        List<Account> userAccounts = user.getAccount();
        assertThat(newAccount.getAccountNumber()).startsWith(SERVICE_CODE);
        assertThat(userAccounts.size()).isEqualTo(2);
        assertThat(userAccounts.stream().anyMatch(x -> x.equals(newAccount))).isTrue();
    }

    @Test
    @DisplayName("계좌 생성 - 실패 - 최대 계설 가능 계좌 개수에 도달했을때")
    void createAccount_Fail_WhenAccountCreationLimitExceeded() {
        //given
        User user = new UserTestDataBuilder().build();
        when(accountRepository.countActiveAccountsByUser(user)).thenReturn(MAX_ACCOUNT_COUNT);

        //when & then
        assertThatThrownBy(() -> accountService.createAccount(user)).isInstanceOf(
                CustomException.class).extracting("errorCode")
            .isEqualTo(ErrorCode.ACCOUNT_CREATION_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("계좌 삭제 - 성공")
    void deleteAccount_Success() {
        //given
        Account account = new AccountTestDataBuilder().withTransactions(
            new ArrayList<>(List.of(new TransactionTestDataBuilder().build()))).build();
        User user = new UserTestDataBuilder().withAccounts(new ArrayList<>(List.of(account)))
            .build();
        int transactionsSizeBefore = account.getTransactions().size();

        //when
        accountService.deleteAccount(user, account);
        int transactionsSizeAfter = account.getTransactions().size();

        //then
        assertThat(account.getStatus()).isEqualTo(Status.DISABLED);
        assertThat(transactionsSizeBefore).isNotZero();
        assertThat(transactionsSizeAfter).isZero();
    }

    @Test
    @DisplayName("계좌 삭제 - 실패 - 사용자와 계좌의 정보가 일치하지 않을 때")
    void deleteAccount_Fail_WhenAccountUserMismatch() {
        //given
        Account userAccount = new AccountTestDataBuilder().withTransactions(
            new ArrayList<>(List.of(new TransactionTestDataBuilder().build()))).build();
        Account otherAccount = new AccountTestDataBuilder().build();
        User user = new UserTestDataBuilder().withAccounts(new ArrayList<>(List.of(userAccount)))
            .build();

        //when & then
        assertThatThrownBy(() -> accountService.deleteAccount(user, otherAccount)).isInstanceOf(
                CustomException.class).extracting("errorCode")
            .isEqualTo(ErrorCode.ACCOUNT_USER_MISMATCH);
    }
}
