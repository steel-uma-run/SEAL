package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.TemplatedCriteria;

public interface TemplatedCriteriaRepository extends JpaRepository<TemplatedCriteria, UUID> {}
