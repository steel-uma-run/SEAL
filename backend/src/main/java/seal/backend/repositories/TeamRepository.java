package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Team;

public interface TeamRepository extends JpaRepository<Team, UUID> {}
