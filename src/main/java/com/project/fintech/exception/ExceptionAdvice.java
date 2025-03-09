package com.project.fintech.exception;

import com.project.fintech.exception.CustomException.CustomExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<CustomExceptionResponse> customExceptionHandler(final CustomException customException) {
        return ResponseEntity.status(customException.getErrorCode().getHttpStatus()).body(CustomException.toResponse(customException));
    }
}
