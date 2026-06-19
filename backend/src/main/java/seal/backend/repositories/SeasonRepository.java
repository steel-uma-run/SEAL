package seal.backend.repositories;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Season;
import seal.backend.enums.Semester;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
  boolean existsBySemesterAndYear(@NotNull Semester semester, Integer year);

  Optional<Season> findBySemesterAndYear(Semester semester, Integer year);
}
