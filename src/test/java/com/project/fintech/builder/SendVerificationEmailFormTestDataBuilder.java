package com.project.fintech.builder;

import com.project.fintech.client.SendVerificationEmailForm;

public class SendVerificationEmailFormTestDataBuilder {

    private String email;
    private String name;
    private String url;
    private final String subject = "회원 가입 인증 메일";

    public SendVerificationEmailFormTestDataBuilder() {
        this.name = "김딸기";
        this.email = "test@test.com";
        this.url = "localhost:8080/verify/email";
    }

    public SendVerificationEmailFormTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public SendVerificationEmailFormTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    public SendVerificationEmailFormTestDataBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public SendVerificationEmailForm build() {
        return SendVerificationEmailForm.builder()
            .email(email)
            .name(name)
            .url(url)
            .build();
    }
}
