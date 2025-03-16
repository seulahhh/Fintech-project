package com.project.fintech.builder;

import com.project.fintech.model.type.Status;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.Transaction;
import com.project.fintech.persistence.entity.User;
import java.util.Collections;
import java.util.List;

public class AccountTestDataBuilder {
    private Long id;
    private User user;
    private List<Transaction> transactions;
    private String accountNumber;
    private Long balance;
    private Status status;

    public AccountTestDataBuilder() {
        this.user = new UserTestDataBuilder().build();
        this.transactions = Collections.emptyList();
        this.accountNumber = "177256143201";
        this.balance = 1000L;
        this.status = Status.ACTIVE;
    }


    public AccountTestDataBuilder withTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        return this;
    }

    public AccountTestDataBuilder withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public AccountTestDataBuilder withBalance(Long balance) {
        this.balance = balance;
        return this;
    }

    public AccountTestDataBuilder withStatus(Status status) {
        this.status = status;
        return this;
    }

    public AccountTestDataBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public Account build() {
        return Account.builder()
            .status(status)
            .transactions(transactions)
            .user(user)
            .balance(balance)
            .accountNumber(accountNumber)
            .build();
    }
}
