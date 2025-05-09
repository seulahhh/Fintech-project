package com.project.fintech.model.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.springframework.data.web.JsonPath;

@Getter
@RequiredArgsConstructor
public enum Message {
    COMPLETE_VERIFY_EMAIL("이메일 인증이 완료"),
    COMPLETE_REGISTERED_OTP("OTP 등록이 완료"),
    COMPLETE_VERIFY_OTP("OTP 인증이 완료"),
    COMPLETE_SEND_EMAIL("메일 전송 완료"),
    COMPLETE_ISSUE_TOKEN("토큰 발급 완료"),
    COMPLETE_ISSUE_SECRETKEY("secret key 발급 완료"),
    COMPLETE_CREAT_ACCOUNT("계좌 계설 완료"),
    COMPLETE_DELETE_ACCOUNT("계좌 삭제 완료"),
    COMPLETE_RETRIEVE_ACCOUNT("사용자 계좌 조회 완료");
    @JsonValue
    private final String message;

    @Override
    public String toString() {
        return message;
    }
}
