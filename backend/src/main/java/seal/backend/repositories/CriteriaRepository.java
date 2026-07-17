package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Criteria;

public interface CriteriaRepository extends JpaRepository<Criteria, UUID> {}
