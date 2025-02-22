package com.project.fintech.persistence.repository;

import com.project.fintech.persistence.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Long> {

}
