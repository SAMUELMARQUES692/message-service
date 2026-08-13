package dev.samuel.message_service.service;

import dev.samuel.message_service.entity.Email;
import dev.samuel.message_service.enums.EmailStatus;
import dev.samuel.message_service.repository.EmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    EmailService emailService;

    @Mock
    EmailRepository emailRepository;

    @Mock
    JavaMailSender javaMailSender;

    @Captor
    ArgumentCaptor<Email> argumentCaptor;

    @Captor
    ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailFrom", "remetente@teste.com");
    }

    @Test
    void sendEmail() {
        Email email = Email.builder()
                .id(1L)
                .emailTo("destinatario@teste.com")
                .emailSubject("Titulo Teste")
                .body("Texto Teste")
                .statusEmail(EmailStatus.PENDING)
                .build();

        Mockito.when(emailRepository.save(email)).thenReturn(email);

        emailService.sendEmail(email);

        Mockito.verify(javaMailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();
        assertEquals(email.getEmailTo(), mensagemEnviada.getTo()[0]);
        assertEquals(email.getEmailSubject(), mensagemEnviada.getSubject());
        assertEquals(email.getBody(), mensagemEnviada.getText());

        Mockito.verify(emailRepository).save(argumentCaptor.capture());
        Email emailSalvo = argumentCaptor.getValue();
        assertEquals(EmailStatus.SENT, emailSalvo.getStatusEmail());
        assertNotNull(emailSalvo.getEnviadoEm());
        assertNotNull(emailSalvo.getEmailFrom()); // confirma que o emailFrom foi setado pelo service
    }

    @Test
    void sendEmailDeveFalharNoEnvio() {
        Email email = Email.builder()
                .id(1L)
                .emailTo("destinatario@teste.com")
                .emailSubject("Titulo Teste")
                .body("Texto Teste")
                .statusEmail(EmailStatus.PENDING)
                .build();

        Mockito.doThrow(new RuntimeException("Falha Teste")).when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));

        Mockito.when(emailRepository.save(email)).thenReturn(email);

        emailService.sendEmail(email);

        Mockito.verify(emailRepository).save(argumentCaptor.capture());
        Email emailSalvo = argumentCaptor.getValue();
        assertEquals(EmailStatus.FAILED, emailSalvo.getStatusEmail());
    }
}