package com.project.fintech.persistence.repository;


import com.project.fintech.persistence.entity.ArchivedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedTransactionRepository extends JpaRepository<ArchivedTransaction, Long> {
}
