package com.project.fintech.builder;

import com.project.fintech.model.type.TransactionType;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.Transaction;
import java.time.LocalDate;
import java.time.LocalTime;

public class TransactionTestDataBuilder {
    private long id;
    private Account account;
    private Long amount;
    private Long recipientAccountId;
    private String memo;
    private TransactionType transactionType;
    private LocalDate transactionDate;
    private LocalTime transactionTime;

    public TransactionTestDataBuilder() {
        this.id = 7L;
        this.account = new AccountTestDataBuilder().build();
        this.amount = 1000L;
        this.recipientAccountId = 10L;
        this.memo = "test transaction";
        this.transactionType = TransactionType.DEPOSIT;
        this.transactionDate = LocalDate.of(2025, 3, 13);
        this.transactionTime = LocalTime.of(12, 20);
    }

    public Transaction build() {
        return Transaction.builder()
            .account(account)
            .amount(amount)
            .recipientAccountId(recipientAccountId)
            .memo(memo)
            .transactionType(transactionType)
            .transactionDate(transactionDate)
            .transactionTime(transactionTime)
            .build();
    }
}

