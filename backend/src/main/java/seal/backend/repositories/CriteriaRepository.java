package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Criteria;
import seal.backend.entities.CriteriaTemplate;

public interface CriteriaRepository extends JpaRepository<Criteria, UUID> {
  void deleteByCriteriaTemplate(CriteriaTemplate template);
}
