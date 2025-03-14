package com.project.fintech.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.application.AuthApplication;
import com.project.fintech.auth.jwt.JwtFilter;
import com.project.fintech.auth.otp.OtpFilter;
import com.project.fintech.auth.springsecurity.CustomAuthenticationEntryPoint;
import com.project.fintech.auth.springsecurity.CustomAuthenticationFilter;
import com.project.fintech.auth.springsecurity.CustomLogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String[] publicEndPoints = {"/auth/login", "/auth/jwt/issue", "/error",
        "/auth/logout", "/", "/swagger-ui/**", "/v3/**", "/swagger-resources/**", "/api-docs/**",
        "/auth/email/verify", "/auth/otp/register"};
    public static final String[] otpVerificationEndPoints = {"/accounts/**"};
    private final AuthApplication authApplication;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * OTP 인증이 필요한 경로에 대한 FilterChain
     */
    @Bean
    public SecurityFilterChain otpSecurityFilterChain(HttpSecurity http) throws Exception {
        OtpFilter otpFilter = new OtpFilter();
        JwtFilter jwtFilter = new JwtFilter(authApplication);
        http.securityMatcher(otpVerificationEndPoints)
            .anonymous(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                auth -> auth
                    .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(otpFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(
            exHandling -> exHandling.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
        AuthenticationManager authenticationManager) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
            authApplication, objectMapper);
        CustomLogoutHandler customLogoutHandler = new CustomLogoutHandler(authApplication,
            objectMapper);
        JwtFilter jwtFilter = new JwtFilter(authApplication);

        http.authenticationProvider(daoAuthenticationProvider(userDetailsService, passwordEncoder))
            .anonymous(AbstractHttpConfigurer::disable);

        customAuthenticationFilter.setAuthenticationManager(authenticationManager);
        customAuthenticationFilter.setFilterProcessesUrl("/auth/login");

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                auth ->
                    auth.requestMatchers(publicEndPoints).permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll().anyRequest()
                        .authenticated())
            .formLogin(AbstractHttpConfigurer::disable)
//            .addFilterBefore(exceptionHandlingFilter, CustomAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout ->
                logout.logoutUrl("/auth/logout").addLogoutHandler(customLogoutHandler)
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpStatus.OK.value());
                    }));
        http.exceptionHandling(
            exHandling -> exHandling.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }



    @Bean
    public AuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
        return configuration.getAuthenticationManager();
    }
}
