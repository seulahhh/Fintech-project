package com.project.fintech.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Message {
    COMPLETE_VERIFY_EMAIL("이메일 인증이 완료"),
    COMPLETE_REGISTERED_OTP("OTP 등록이 완료"),
    COMPLETE_VERIFY_OTP("OTP 인증이 완료"),
    ;
    private final String message;

    @Override
    public String toString() {
        return this.message;
    }
}
