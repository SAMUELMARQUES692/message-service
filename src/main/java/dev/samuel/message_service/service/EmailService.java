package dev.samuel.message_service.service;

import dev.samuel.message_service.entity.Email;
import dev.samuel.message_service.enums.EmailStatus;
import dev.samuel.message_service.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Transactional
    public void sendEmail(Email email) {
        email.setEmailFrom(emailFrom);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email.getEmailTo());
            message.setSubject(email.getEmailSubject());
            message.setText(email.getBody());
            javaMailSender.send(message);
            email.setStatusEmail(EmailStatus.SENT);
            email.setEnviadoEm(LocalDateTime.now());

        } catch (Exception exception) {
            email.setStatusEmail(EmailStatus.FAILED);
            log.error("Erro ao enviar email para {}: {}", email.getEmailTo(), exception.getMessage());
        }

        emailRepository.save(email);
    }

}
