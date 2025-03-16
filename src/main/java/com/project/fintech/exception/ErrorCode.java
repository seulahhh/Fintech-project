package com.project.fintech.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", "AUTH-001", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXIST("이미 가입된 이메일 입니다.", "AUTH-002", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("이메일 인증이 완료되지 않았습니다.", "AUTH-003", HttpStatus.BAD_REQUEST),
    INVALID_OTP_CODE("OTP 인증에 실패 하였습니다.", "AUTH-004", HttpStatus.BAD_REQUEST),
    OTP_NOT_REGISTERED("OTP 인증을 위한 등록을 먼저 진행해주세요.", "AUTH-005", HttpStatus.BAD_REQUEST),
    OTP_SECRET_KEY_NOT_FOUND("사용자의 OTP Secret Key가 존재하지 않습니다.", "AUTH-006", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("토큰이 유효하지 않습니다.", "AUTH-007", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("토큰이 만료 되었습니다.", "AUTH-008", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("토큰이 존재하지 않습니다.", "AUTH-009", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_USER_MISMATCH("Refresh Token의 사용자가 일치하지 않습니다.", "AUTH-010",
        HttpStatus.BAD_REQUEST),
    LOGIN_REQUEST_FAIL("인증에 실패하였습니다.", "AUTH-011", HttpStatus.BAD_REQUEST),
    OTP_ATTEMPT_EXCEEDED("해당 OTP 코드에 대한 인증 횟수가 초과 되었습니다.", "AUTH-012", HttpStatus.BAD_REQUEST),

    IO_OPERATION_FAILED("입출력 작업 중 오류가 발생했습니다.", "IO-001", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생하였습니다.", "SERVER", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_NOT_FOUND("계좌를 찾을 수 없습니다.", "ACCOUNT-001", HttpStatus.BAD_REQUEST),
    ACCOUNT_CREATION_LIMIT_EXCEEDED("최대 계설 가능 개수를 초과하였습니다", "ACCOUNT-002", HttpStatus.BAD_REQUEST),
    ACCOUNT_USER_MISMATCH(
        "계좌번호와 사용자의 정보가 일치하지 않습니다.", "ACCUONT-003", HttpStatus.BAD_REQUEST),
    ACCOUNT_BALANCE_NOT_ZERO("게좌의 잔액이 남아있습니다.", "ACCOUNT-004", HttpStatus.BAD_REQUEST);

    private final String detail;
    private final String code;
    private final HttpStatus httpStatus;
}
