package dev.samuel.message_service.consumer;

import dev.samuel.message_service.entity.Email;
import dev.samuel.message_service.enums.EmailStatus;
import dev.samuel.message_service.response.UsuarioEvent;
import dev.samuel.message_service.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @InjectMocks
    EmailConsumer emailConsumer;

    @Mock
    EmailService emailService;

    @Captor
    ArgumentCaptor<Email> argumentCaptor;


    @Test
    void listenUsuarioCadastrado() {
        UsuarioEvent event = UsuarioEvent.builder()
                .email("testeemail@gmail.com")
                .nome("Nome Teste")
                .build();

        emailConsumer.listenUsuarioCadastrado(event);

        Mockito.verify(emailService).sendEmail(argumentCaptor.capture());
        Email email = argumentCaptor.getValue();
        assertEquals(event.email(), email.getEmailTo());
        assertEquals(EmailStatus.PENDING, email.getStatusEmail());
        assertEquals("Bem-vindo(a) à Prolab!", email.getEmailSubject());
        assertTrue(email.getBody().contains(event.nome()));
    }

    @Test
    void listenUsuarioAtualizado() {
        UsuarioEvent event = UsuarioEvent.builder()
                .email("testeemail@gmail.com")
                .nome("Nome Teste")
                .build();

        emailConsumer.listenUsuarioAtualizado(event);

        Mockito.verify(emailService).sendEmail(argumentCaptor.capture());
        Email email = argumentCaptor.getValue();
        assertEquals(event.email(), email.getEmailTo());
        assertEquals(EmailStatus.PENDING, email.getStatusEmail());
        assertEquals("Atualização de Login!", email.getEmailSubject());
        assertTrue(email.getBody().contains(event.nome()));
    }
}