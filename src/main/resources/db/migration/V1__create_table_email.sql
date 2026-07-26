CREATE TABLE emails(
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    email_from VARCHAR(255) NOT NULL,
    email_to VARCHAR(255) NOT NULL,
    email_subject VARCHAR(255) NOT NULL,
    body TEXT,
    enviado_em TIMESTAMP,
    status_email VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);