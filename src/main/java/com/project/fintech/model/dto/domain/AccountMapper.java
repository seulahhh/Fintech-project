package com.project.fintech.model.dto.domain;

import com.project.fintech.persistence.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source="user.email", target = "userEmail")
    AccountDto toAccountDto(Account account);
}