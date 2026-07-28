package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.TeamTemplate;

public interface TeamTemplateRepository extends JpaRepository<TeamTemplate, UUID> {}
