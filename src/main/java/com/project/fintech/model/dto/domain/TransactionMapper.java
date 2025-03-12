package com.project.fintech.model.dto.domain;

import com.project.fintech.persistence.entity.ArchivedTransaction;
import com.project.fintech.persistence.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "id", ignore = true)
    ArchivedTransaction toArchivedTransactions(Transaction transaction);
}
