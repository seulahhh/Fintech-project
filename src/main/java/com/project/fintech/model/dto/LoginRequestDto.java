package com.project.fintech.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "LoginRequestDto", description = "로그인 요청 DTO")
public class LoginRequestDto {

    @Schema(example = "user@example.com")
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    @NotBlank(message = "이메일 입력값이 누락되었습니다.")
    private String email;

    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDA4NDYwOT...")
    @Size(min = 8, message = "비밀번호는 8자 이상입니다.")
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;
}
