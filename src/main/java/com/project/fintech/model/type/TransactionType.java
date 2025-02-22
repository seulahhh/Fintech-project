package com.project.fintech.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    WITHDRAW("인출"), TRANSFER("송금"), DEPOSIT("입금");

    private final String description;
}
