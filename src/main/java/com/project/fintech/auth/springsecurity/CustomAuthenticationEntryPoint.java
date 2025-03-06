package com.project.fintech.auth.springsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.CustomException.CustomExceptionResponse;
import com.project.fintech.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        CustomExceptionResponse customExceptionResponse = CustomException.toResponse(
            new CustomException(ErrorCode.LOGIN_REQUEST_FAIL));
        String customExceptionResponseJson = objectMapper.writeValueAsString(customExceptionResponse);
        response.getWriter().write(customExceptionResponseJson);
        response.getWriter().flush();
    }
}