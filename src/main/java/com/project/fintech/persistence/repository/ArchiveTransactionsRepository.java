package com.project.fintech.persistence.repository;


import com.project.fintech.persistence.entity.ArchivedTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchiveTransactionsRepository extends JpaRepository<Long, ArchivedTransactions> {
}
