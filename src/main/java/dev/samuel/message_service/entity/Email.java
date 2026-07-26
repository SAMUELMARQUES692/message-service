package dev.samuel.message_service.entity;

import dev.samuel.message_service.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emailFrom;

    @Column(nullable = false)
    private String emailTo;

    @Column(nullable = false)
    private String emailSubject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus statusEmail;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
