package com.project.fintech.auth.jwt;

import com.project.fintech.application.AuthApplication;
import com.project.fintech.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AuthApplication authApplication;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (request.getHeader("Authorization") == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        log.info("JWT Filter 동작");
        log.info("jwt token: {}", token);
        try {
            authApplication.executeJwtAuthentication(token);
        } catch (CustomException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw e;
        }
        filterChain.doFilter(request, response);
    }
}


