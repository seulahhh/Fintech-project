package com.project.fintech.builder;

import com.project.fintech.model.dto.RegisterRequestDto;

public class RegisterRequestDtoTestDataBuilder {
    private String email;
    private String password;
    private String name;
    private String phone;

    public RegisterRequestDtoTestDataBuilder() {
        this.email = "random@random.com";
        this.password = "12341234";
        this.name = "purple";
        this.phone = "01012341212";
    }

    public RegisterRequestDtoTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    public RegisterRequestDtoTestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    public RegisterRequestDtoTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    public RegisterRequestDtoTestDataBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public RegisterRequestDto build() {
        return RegisterRequestDto.builder()
            .email(email)
            .password(password)
            .name(name)
            .phone(phone)
            .build();
    }
}
