package com.project.fintech.api;

import com.project.fintech.application.AccountApplication;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.model.dto.domain.AccountDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountsController {

    private final AccountApplication accountApplication;

    /**
     * 계좌 계설(생성)
     * @return 생성한 Account의 DTO
     */
    @PostMapping("/accounts")
    public ResponseEntity<ResponseDto<AccountDto>> createAccount() {
        return ResponseEntity.ok(accountApplication.executeCreateAccount());
    }

    /**
     * 계좌 삭제(Soft Delete)
     * @param accountId
     * @return
     */
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseDto<AccountDto>> deleteAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountApplication.executeDeleteAccount(accountId));
    }

    /**
     * 사용자가 보유중인 모든 계좌 조회
     * @return Account DTO List
     */
    @GetMapping("/accounts")
    public ResponseEntity<ResponseDto<List<AccountDto>>> getAccounts() {
        return ResponseEntity.ok(accountApplication.executeRetrieveUserAccounts());
    }

    /**
     * 사용자의 특정 계좌 조회
     * @param accountId
     * @return Account DTO
     */
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseDto<AccountDto>> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountApplication.executeRetrieveSingleAccount(accountId));
    }
}
