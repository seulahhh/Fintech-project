package com.project.fintech.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }

    public static CustomExceptionResponse toResponse(CustomException customException) {
        return new CustomExceptionResponse(customException.errorCode.getCode(),
            customException.errorCode.getDetail());
    }

    @Getter // json 파싱을 위함
    @AllArgsConstructor
    @Builder
    public static class CustomExceptionResponse {
        private String code;
        private String detail;
    }
}
