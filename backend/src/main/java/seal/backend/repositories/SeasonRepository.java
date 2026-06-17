package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Season;
import seal.backend.enums.Semester;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
  boolean existsBySemesterAndYear(Semester semester, Integer year);
}
