package com.project.fintech.auth.springsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.model.dto.ResponseDto;
import com.project.fintech.application.AuthApplication;
import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import com.project.fintech.model.dto.LoginRequestDto;
import com.project.fintech.model.dto.TokenPairDto;
import com.project.fintech.model.type.Message;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthApplication authApplication;
    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {

        try {
            LoginRequestDto loginRequestDto = objectMapper.readValue(request.getInputStream(),
                LoginRequestDto.class);
            logger.info(loginRequestDto.getEmail());
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IO_OPERATION_FAILED);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();

        response.setContentType("application/json;charset=UTF-8");
        TokenPairDto tokenPairDto = authApplication.issueTokenPair(authResult.getName());
        ResponseDto<TokenPairDto> responseDto = ResponseDto.<TokenPairDto>builder()
            .code(HttpServletResponse.SC_OK).data(tokenPairDto)
            .message(Message.COMPLETE_ISSUE_TOKEN).build();
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed)
        throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        throw new CustomException(ErrorCode.LOGIN_REQUEST_FAIL);
    }
}


