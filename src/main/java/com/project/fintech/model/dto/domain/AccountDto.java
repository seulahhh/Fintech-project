package com.project.fintech.model.dto.domain;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String userEmail;
    private String accountNumber;
    private Long balance;
}
