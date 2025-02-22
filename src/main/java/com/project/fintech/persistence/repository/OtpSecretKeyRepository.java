package com.project.fintech.persistence.repository;

import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpSecretKeyRepository extends JpaRepository<OtpSecretKey, Long> {
    Optional<OtpSecretKey> findByUser(User user);
}
