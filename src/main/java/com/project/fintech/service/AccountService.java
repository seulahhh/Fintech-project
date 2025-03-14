package com.project.fintech.service;

import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.domain.TransactionMapper;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.ArchivedTransaction;
import com.project.fintech.persistence.entity.Transaction;
import com.project.fintech.persistence.entity.User;
import com.project.fintech.persistence.repository.AccountRepository;
import com.project.fintech.persistence.repository.ArchivedTransactionRepository;
import com.project.fintech.persistence.repository.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final Long MAX_ACCOUNT_COUNT = 3L;
    private final String SERVICE_CODE = "177";

    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final ArchivedTransactionRepository archivedTransactionRepository;

    /**
     * email 로 User 가져오기
     * @param email
     * @return User
     */
    @Transactional
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * account id로 Account 가져오기
     *
     * @param accountId
     * @return Account
     */
    @Transactional
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    /**
     * 계좌 생성하기
     *
     * @param user
     * @return created account
     */
    @Transactional
    public Account createAccount(User user) {
        long accountCounts = accountRepository.countByUser(user);
        if (accountCounts >= MAX_ACCOUNT_COUNT) {
            throw new CustomException(ErrorCode.ACCOUNT_CREATION_LIMIT_EXCEEDED);
        }
        Account account = Account.builder().accountNumber(createAccountNumber()).user(user).build();
        user.getAccount().add(account);
        return account;
    }

    /**
     * 게좌 삭제하기(Soft delete)
     *
     * @param user
     * @param account
     */
    @Transactional
    public void deleteAccount(User user, Account account) {
        if (user.getAccount().stream().noneMatch(userAccount -> userAccount == account)) {
            throw new CustomException(ErrorCode.ACCOUNT_USER_MISMATCH);
        }
        account.disabled();
        moveTransactionsToArchive(account);
    }

    /**
     * 해당 계좌번호의 transaction을 Archived transaction 테이블로 옮기기
     *
     * @param account
     */
    private void moveTransactionsToArchive(Account account) {
        List<Transaction> transactionsList = account.getTransactions();

        if (!transactionsList.isEmpty()) {
            List<ArchivedTransaction> archivedTransactionList = transactionsList.stream()
                .map(transactionMapper::toArchivedTransactions).toList();
            archivedTransactionRepository.saveAll(archivedTransactionList);
            account.getTransactions().clear();
        }
    }

    /**
     * Modulo11 방식을 이용하여 checkDigit 생성 (계좌번호 맨 끝자리를 checkDigit으로 넣어 보안 강화)
     *
     * @return check digit
     */
    private String createCheckDigit(String middleNum) {
        int sum = 0;
        int weight = 2;

        for (int i = middleNum.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(middleNum.charAt(i));
            sum += digit * weight;
            weight++;
        }

        int remainder = sum % 11;
        int checkDigitValue = 11 - remainder;

        char checkDigit;
        if (checkDigitValue == 10) {
            checkDigit = 'X';
        } else if (checkDigitValue == 11) {
            checkDigit = '0';
        } else {
            checkDigit = Character.forDigit(checkDigitValue, 10);
        }

        return String.valueOf(checkDigit);
    }

    /**
     * 계좌번호 생성 Util 메서드
     *
     * @return account number
     */
    @Transactional
    public String createAccountNumber() {
        SecureRandom secureRandom = new SecureRandom();
        long randomNumber = 10_000_000L + secureRandom.nextLong(90_000_000L);
        String middleNums = String.valueOf(randomNumber);
        String checkDigit = createCheckDigit(middleNums);

        return SERVICE_CODE + middleNums + checkDigit;
    }
}
