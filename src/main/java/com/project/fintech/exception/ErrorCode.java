package com.project.fintech.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND_USER("사용자를 찾을 수 없습니다.","AUTH-001", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST_EMAIL("이미 가입된 이메일 입니다.", "AUTH-002", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("이메일 인증이 완료되지 않았습니다.", "AUTH-003", HttpStatus.BAD_REQUEST),
    NOT_VALID_OTP_CODE("OTP 인증에 실패 하였습니다.", "AUTH-004", HttpStatus.BAD_REQUEST),
    OTP_NOT_REGISTERED("OTP 인증을 위한 등록을 먼저 진행해주세요.", "AUTH-005", HttpStatus.BAD_REQUEST)
    ;

    private final String detail;
    private final String code;
    private final HttpStatus httpStatus;
}
