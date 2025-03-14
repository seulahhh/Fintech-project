package com.project.fintech.persistence.repository;

import com.project.fintech.model.type.Status;
import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    long countByUserAndStatus(User user, Status status);
    List<Account> findByUserAndStatus(User user, Status status);

    default List<Account> findActiveAccounts(User user) {
        return findByUserAndStatus(user, Status.ACTIVE);
    }

    default Long countActiveAccountsByUser(User user) {
        return countByUserAndStatus(user, Status.ACTIVE);
    }
}
