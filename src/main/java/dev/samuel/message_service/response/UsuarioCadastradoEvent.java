package dev.samuel.message_service.response;

public record UsuarioCadastradoEvent(
        String nome,
        String email
) {}
