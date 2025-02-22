package com.project.fintech.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendVerificationEmailForm {
    private final String subject = "회원 가입 인증 메일";
    private String email;
    private String name;
    private String url;
}
