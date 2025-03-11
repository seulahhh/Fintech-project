package com.project.fintech.auth.otp;

import com.project.fintech.exception.CustomException;
import com.project.fintech.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class OtpFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        log.info("starting OTP Filter..");
        Object otpVerified = request.getSession().getAttribute("OTPVerified");
        if (otpVerified == null || !(Boolean) otpVerified) {
            throw new CustomException(ErrorCode.INVALID_OTP_CODE);
        }

        filterChain.doFilter(request, response);
    }
}
