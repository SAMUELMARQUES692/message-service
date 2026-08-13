package dev.samuel.message_service.response;

import lombok.Builder;

@Builder
public record UsuarioEvent(
        String nome,
        String email
) {}
