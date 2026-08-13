# message-service

Serviço de **notificações por e-mail** do ecossistema Prolab. Não expõe API REST: é um **worker orientado a eventos** que consome mensagens do RabbitMQ e dispara e-mails via SMTP, registrando cada envio no banco.

## Arquitetura

Faz parte de um ecossistema de três microsserviços:

| Serviço | Papel | Repositório |
|---|---|---|
| **auth-service** | Emite o JWT, gerencia usuários e **publica** eventos de usuário no RabbitMQ | https://github.com/SAMUELMARQUES692/auth-service |
| **ProlabSystem** | API de domínio (gestão de resíduos) | https://github.com/SAMUELMARQUES692/ProlabSystem |
| **message-service** (este) | **Consome** os eventos do RabbitMQ e envia e-mails | — |

```
  auth-service ──publica UsuarioEvent──► RabbitMQ (prolab.exchange)
                                              │
                          ┌───────────────────┴───────────────────┐
                          ▼                                       ▼
                   usuario.queue                       usuario.atualizado.queue
                   (cadastro)                           (atualização)
                          │                                       │
                          └───────────────────┬───────────────────┘
                                               ▼
                                        message-service
                                              │
                                              ├─► envia e-mail (SMTP)
                                              └─► persiste o registro (status PENDING → SENT/FAILED)
```

O acoplamento é assíncrono: o `auth-service` não conhece o `message-service`. Ele apenas publica um evento; quem quiser reagir se inscreve na fila. Isso mantém o cadastro de usuário rápido e resiliente — se o envio de e-mail falhar, não derruba o fluxo de autenticação.

## Stack

- Java 17
- Spring Boot 4.1.0
- Spring AMQP (RabbitMQ)
- Spring Mail (JavaMailSender / SMTP)
- PostgreSQL + Flyway
- Lombok

## Como funciona

1. Escuta duas filas do RabbitMQ: `usuario.queue` (cadastro) e `usuario.atualizado.queue` (atualização).
2. Ao receber um `UsuarioEvent` (nome + e-mail), monta o e-mail correspondente:
    - Cadastro → "Bem-vindo(a) à Prolab!"
    - Atualização → "Atualização de Login!"
3. Envia via SMTP e persiste a entidade `Email` com o status resultante:
    - `PENDING` → ao criar
    - `SENT` → envio ok (grava `enviadoEm`)
    - `FAILED` → falha no envio (logada, sem derrubar o consumo)

## Como rodar

### Pré-requisitos
- JDK 17+
- PostgreSQL
- RabbitMQ (configurado para CloudAMQP com SSL) — a mesma instância usada pelo `auth-service`
- Uma conta SMTP (o projeto está configurado para Gmail)

### Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DATABASE_URL` | JDBC do Postgres, ex.: `jdbc:postgresql://localhost:5432/message` |
| `DATABASE_USERNAME` | usuário do banco |
| `DATABASE_PASSWORD` | senha do banco |
| `RABBIT_ADDRESSES` | endereço(s) do broker RabbitMQ |
| `RABBIT_USERNAME` | usuário do RabbitMQ |
| `RABBIT_PASSWORD` | senha do RabbitMQ |
| `RABBIT_VIRTUAL_HOST` | virtual host do RabbitMQ |
| `SMTP_USERNAME` | e-mail remetente (usuário SMTP) |
| `SMTP_PASSWORD` | senha/app password do SMTP |

> Para Gmail, use uma **App Password** (não a senha da conta) com verificação em duas etapas ativada.

### Executando

```bash
./mvnw spring-boot:run
```

Sobe em `http://localhost:8082`. Não há endpoints REST — o serviço fica escutando o RabbitMQ. Para testar o fluxo ponta a ponta, cadastre um usuário no `auth-service` e verifique a caixa de entrada + o registro na tabela `email`.

## Mensageria (RabbitMQ)

- **Exchange:** `prolab.exchange` (topic)

| Evento | Routing Key | Fila |
|---|---|---|
| Cadastro de usuário | `usuario.mensagem` | `usuario.queue` |
| Atualização de usuário | `usuario.atualizado` | `usuario.atualizado.queue` |

Cada tipo de evento tem sua própria fila e routing key — isso evita que os dois listeners (`listenUsuarioCadastrado`, `listenUsuarioAtualizado`) disputem mensagens da mesma fila, garantindo que cada evento sempre dispare o e-mail correto (boas-vindas vs. atualização de dados).

O payload (`UsuarioEvent`) é desserializado de JSON automaticamente pelo `JacksonJsonMessageConverter`.

## Organização do código

```
dev.samuel.message_service
├── configuration   # RabbitMQConfig
├── consumer        # EmailConsumer (@RabbitListener)
├── entity          # Email
├── enums           # EmailStatus (PENDING, SENT, FAILED)
├── dto (response)  # UsuarioEvent (payload consumido)
├── repository      # EmailRepository
└── service         # EmailService (envio + persistência)
```

## Persistência

A tabela `email` guarda o histórico de notificações (destinatário, assunto, corpo, remetente, status, data de envio), útil para auditoria e reprocessamento.

---

## Testes

```bash
./mvnw test
```

A suíte cobre duas camadas com Mockito, isolando a lógica de negócio das dependências externas (SMTP, banco, RabbitMQ):

- **Service** — testes unitários do `EmailService`, cobrindo o envio bem-sucedido (status `SENT`, `enviadoEm` preenchido, `emailFrom` corretamente atribuído a partir da configuração) e a falha de envio (status `FAILED`, registro preservado para auditoria mesmo quando o SMTP falha).
- **Consumer** — testes unitários do `EmailConsumer`, confirmando que cada listener (`listenUsuarioCadastrado`, `listenUsuarioAtualizado`) monta o e-mail correto a partir do evento recebido, com assunto e corpo específicos para cada tipo de notificação.

Como o serviço não expõe endpoints REST, não há testes de Controller — a cobertura se concentra nas duas camadas que realmente contêm lógica de negócio.

Projeto de portfólio — Samuel Marques.
