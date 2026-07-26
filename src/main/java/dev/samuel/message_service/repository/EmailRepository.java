package dev.samuel.message_service.repository;

import dev.samuel.message_service.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
}
