package com.project.fintech.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    USER("ROLE_USER"), ADMIN("ROLE_ADMIN"), PENDING("ROLE_PENDING");

    private final String title;


    @Override
    public String getAuthority() {
        return this.title;
    }
}
