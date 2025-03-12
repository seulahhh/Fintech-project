package com.project.fintech.builder;

import com.project.fintech.persistence.entity.Account;
import com.project.fintech.persistence.entity.OtpSecretKey;
import com.project.fintech.persistence.entity.User;
import java.util.Collections;
import java.util.List;

public class UserTestDataBuilder {

    private Long id;
    private String password;
    private String email;
    private String name;
    private String phone;
    private OtpSecretKey otpSecretKey;
    private List<Account> accounts;
    private Boolean isVerifiedEmail;
    private Boolean isOtpRegistered;

    public UserTestDataBuilder() {
        this.email = "random@random.com";
        this.password = "encodedPassword";
        this.phone = "01012341212";
        this.name = "purple";
        this.otpSecretKey = OtpSecretKey.builder().secretKey("THISISSECRETKEY").build();
        this.accounts = Collections.emptyList();
        this.isOtpRegistered = false;
        this.isVerifiedEmail = false;
    }

    public UserTestDataBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserTestDataBuilder withPassword(List<Account> account) {
        this.accounts = account;
        return this;
    }

    public UserTestDataBuilder withIsVerifiedEmail(boolean isVerifiedEmail) {
        this.isVerifiedEmail = isVerifiedEmail;
        return this;
    }

    public UserTestDataBuilder withIsOtpRegistered(boolean isOtpRegistered) {
        this.isOtpRegistered = isOtpRegistered;
        return this;
    }

    public UserTestDataBuilder withOtpSecretKey(OtpSecretKey otpSecretKey) {
        this.otpSecretKey = otpSecretKey;
        return this;
    }

    public UserTestDataBuilder withAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }

    public User build() {
        return User.builder()
            .id(id)
            .email(email)
            .account(accounts)
            .password(password)
            .email(email)
            .name(name)
            .phone(phone)
            .otpSecretKey(otpSecretKey)
            .isVerifiedEmail(isVerifiedEmail)
            .isOtpRegistered(isOtpRegistered)
            .build();
    }
}
