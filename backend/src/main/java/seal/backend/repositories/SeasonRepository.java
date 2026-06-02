package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Season;

public interface SeasonRepository extends JpaRepository<Season, UUID> {}
