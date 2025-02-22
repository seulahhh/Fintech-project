package com.project.fintech.client;

import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EmailRequest {
    private Sender sender;
    private List<Recipient> to;
    private String htmlContent;
    private String subject;

    @Data
    @Builder
    public static class Recipient {
        private String email;
        private String name;
    }

    @Data
    @Builder
    public static class Sender {
        private String email;
        private String name;
    }
}
// json 형식 유지 위해 중첩 클래스 사용