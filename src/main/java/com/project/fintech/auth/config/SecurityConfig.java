package com.project.fintech.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.fintech.application.AuthApplication;
import com.project.fintech.auth.constants.SecurityPathConstants;
import com.project.fintech.auth.jwt.JwtFilter;
import com.project.fintech.auth.springsecurity.CustomAuthenticationFilter;
import com.project.fintech.auth.springsecurity.CustomLogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

    private final AuthApplication authApplication;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
        AuthenticationManager authenticationManager) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(
            authApplication, objectMapper);
        CustomLogoutHandler customLogoutHandler = new CustomLogoutHandler(authApplication,
            objectMapper);
//        OtpFilter otpFilter = new OtpFilter();

        http.authenticationProvider(daoAuthenticationProvider(userDetailsService, passwordEncoder));
        customAuthenticationFilter.setAuthenticationManager(authenticationManager);
        customAuthenticationFilter.setFilterProcessesUrl("/auth/login");

        http.csrf(AbstractHttpConfigurer::disable).sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                auth ->
                    auth.requestMatchers(SecurityPathConstants.publicEndPoints).permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll().anyRequest()
                        .authenticated()).formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout ->
                logout.logoutUrl("/auth/logout").addLogoutHandler(customLogoutHandler)
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpStatus.OK.value());
                    }));

        return http.build();
    }

    @Bean
    @Lazy
    public AuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    @Lazy
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
        throws Exception {
        return configuration.getAuthenticationManager();
    }
}
