package com.project.fintech.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequestDto {
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    @NotBlank(message = "이메일 입력은 필수 입니다.")
    private String email;

    @Size(min = 8, message = "비밀번호는 8자 이상입니다.")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "이름 입력은 필수 입니다.")
    private String name;

    @NotBlank(message = "휴대폰 번호 입력은 필수 입니다.")
    private String phone;
}
