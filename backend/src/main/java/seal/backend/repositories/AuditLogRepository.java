package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.AuditLog;

public interface AuditLogRepository<T extends AuditLog> extends JpaRepository<T, UUID> {}
