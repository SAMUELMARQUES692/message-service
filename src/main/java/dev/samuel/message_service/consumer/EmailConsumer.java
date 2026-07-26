package dev.samuel.message_service.consumer;

import dev.samuel.message_service.entity.Email;
import dev.samuel.message_service.enums.EmailStatus;
import dev.samuel.message_service.response.UsuarioCadastradoEvent;
import dev.samuel.message_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "usuario.cadastrado.queue")
    public void listenUsuarioCadastrado(@Payload UsuarioCadastradoEvent evento) {
        Email email = Email.builder()
                .emailTo(evento.email())
                .emailSubject("Bem-vindo(a) à Prolab!")
                .body("Olá " + evento.nome() + ",\n\nSeu cadastro foi realizado com sucesso!")
                .statusEmail(EmailStatus.PENDING)
                .build();

        emailService.sendEmail(email);
    }
}
