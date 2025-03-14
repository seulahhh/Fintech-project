package com.project.fintech.persistence.repository;

import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    long countByUser(User user);
}
