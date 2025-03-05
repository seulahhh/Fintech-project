package com.project.fintech.auth.springsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.application.AuthApplication;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.LogoutRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final AuthApplication authApplication;
    private final ObjectMapper objectMapper;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        try {
            LogoutRequestDto logoutRequestDto = objectMapper.readValue(request.getInputStream(),
                LogoutRequestDto.class);
            authApplication.processTokenWhenLogout(logoutRequestDto);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IO_OPERATION_FAILED);
        }

        SecurityContextHolder.clearContext();
    }
}
