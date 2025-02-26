package com.project.fintech.client;

import com.project.fintech.client.EmailRequest.Recipient;
import com.project.fintech.client.EmailRequest.Sender;
import com.project.fintech.model.dto.RegisterRequestDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;
    Context context = new Context();

    @Qualifier("brevoWebClient")
    private final WebClient brevoWebClient;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    /**
     * Brevo API 호출을 위한 EmailRequest 생성 및 세팅
     *
     * @param sendVerificationEmailForm
     * @return EmailRequest
     */
    public EmailRequest createEmailRequest(SendVerificationEmailForm sendVerificationEmailForm) {
        // Email Contents(body) 세팅
        Sender sender = Sender.builder().email(senderEmail).name(senderName).build();

        List<Recipient> recipient = List.of(
            Recipient.builder().email(sendVerificationEmailForm.getEmail())
                .name(sendVerificationEmailForm.getName()).build());
        String emailVerificationLink = sendVerificationEmailForm.getUrl();

        context.setVariable("link", emailVerificationLink);
        String htmlContent = templateEngine.process("email-template", context);
        String subject = sendVerificationEmailForm.getSubject();

        return EmailRequest.builder().sender(sender).to(recipient).htmlContent(htmlContent)
            .subject(subject).build();
    }

    /**
     * Brevo API를 호출하여 Email 전송
     *
     * @param sendVerificationEmailForm
     * @return api 호출 결과
     */
    public String sendSignupVerificationMail(SendVerificationEmailForm sendVerificationEmailForm) {
        EmailRequest emailRequest = createEmailRequest(sendVerificationEmailForm);

        brevoWebClient.post().uri("/smtp/email").bodyValue(emailRequest).retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                    System.err.println("Brevo API 오류 응답: " + errorBody);
                    return Mono.error(new RuntimeException("Brevo API 오류: " + errorBody));
                })).bodyToMono(String.class).block();

        return emailRequest.getHtmlContent();
    }

    /**
     * RegisterRequestDto 을 받으면 SendVerificationEmailForm으로 변환해주어 이메일을 보낼 수 있게 해줌
     *
     * @param registerRequestDto
     * @param emailVerificationUri
     * @return sendVerificationEmailForm
     */
    public SendVerificationEmailForm toSendVerificationEmailForm(
        RegisterRequestDto registerRequestDto, String emailVerificationUri) {
        return SendVerificationEmailForm.builder().email(registerRequestDto.getEmail())
            .name(registerRequestDto.getName()).url(emailVerificationUri).build(); // todo Link 수정
    }

}