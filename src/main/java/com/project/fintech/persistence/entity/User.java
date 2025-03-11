package com.project.fintech.persistence.entity;

import com.project.fintech.model.type.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="users")
public class User extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String password;

    @Column (unique = true)
    private String email;

    private String name;

    private String phone;

    @OneToOne (mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OtpSecretKey otpSecretKey;

    @OneToMany (mappedBy = "user")
    private List<Account> account;

    @Builder.Default
    private Boolean isVerifiedEmail = false;

    @Builder.Default
    private Boolean isOtpRegistered = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!isVerifiedEmail || !isOtpRegistered) {
            return List.of(Role.PENDING);
        }
        return List.of(Role.USER);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public void setVerifiedEmail(Boolean verifiedEmail) {
        isVerifiedEmail = verifiedEmail;
    }

    public void setOtpRegistered(Boolean otpRegistered) {
        isOtpRegistered = otpRegistered;
    }

    public void setUserSecretKey(OtpSecretKey otpSecretKey) {
        this.otpSecretKey = otpSecretKey;
    }
}
