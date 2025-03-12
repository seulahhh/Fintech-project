package com.project.fintech.model.dto.domain;

import com.project.fintech.persistence.entity.ArchivedTransactions;
import com.project.fintech.persistence.entity.Transactions;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TransactionsMapper {
    ArchivedTransactions toArchivedTransactions(Transactions transactions);
}
