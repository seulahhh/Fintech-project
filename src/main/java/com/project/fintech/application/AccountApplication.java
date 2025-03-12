package com.project.fintech.application;

import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.domain.AccountDto;
import com.project.fintech.model.dto.domain.AccountMapper;
import com.project.fintech.model.type.Message;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.service.AccountService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountApplication {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    /**
     * 계좌 생성 흐름
     *
     * @return
     */
    @Transactional
    public ResponseDto<AccountDto> executeCreateAccount() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = accountService.getUserByEmail(userEmail);

        Account account = accountService.createAccount(user);

        return ResponseDto.<AccountDto>builder().code(HttpServletResponse.SC_OK)
            .message(Message.COMPLETE_CREAT_ACCOUNT).data(accountMapper.toAccountDto(account))
            .build();
    }

    /**
     * 계좌 삭제 흐름
     *
     * @param accountId
     * @return
     */
    @Transactional
    public ResponseDto<AccountDto> executeSoftDeleteAccount(Long accountId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = accountService.getUserByEmail(userEmail);
        Account account = accountService.getAccountById(accountId);

        accountService.deleteAccount(user, account);

        return ResponseDto.<AccountDto>builder().code(HttpServletResponse.SC_OK)
            .message(Message.COMPLETE_DELETE_ACCOUNT)
            .data(accountMapper.toAccountDto(account)).build();
    }
}
