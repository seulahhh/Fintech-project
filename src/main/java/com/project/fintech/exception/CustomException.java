package com.project.fintech.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDetail());
        this.errorCode = errorCode;
    }

    public CustomExceptionResponse toResponse() {
        return new CustomExceptionResponse(this.errorCode);
    }

    @Getter // json 파싱을 위함
    @AllArgsConstructor
    @Builder
    public static class CustomExceptionResponse {
        private ErrorCode errorCode;
    }
}
