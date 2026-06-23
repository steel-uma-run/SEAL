package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.CriteriaTemplate;

public interface CriteriaTemplateRepository extends JpaRepository<CriteriaTemplate, UUID> {}
