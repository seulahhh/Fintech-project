package com.project.fintech.model.dto;

import com.project.fintech.model.type.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "ResponseDto<T>", description = "공통 응답 DTO")
public class ResponseDto<T> {
    private int code;
    private Message message;
    private T data;
}