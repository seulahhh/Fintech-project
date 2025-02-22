package com.project.fintech.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {
    // todo custom 예외 발생 시 response 설정
    // todo springboot valide 결과 MethodArgumentNotValidException에 대한 response , Advice(전역) 설정
}
