package com.project.fintech.persistence.entity;

import com.project.fintech.model.type.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="archived_transactions")
public class ArchivedTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private Long amount;

    private Long recipient_account_id;

    private String memo;

    private TransactionType transactionType;

    private LocalDate transactionDate;

    private LocalTime transactionTime;
}
