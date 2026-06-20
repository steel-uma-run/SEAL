package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Team;

public interface TeamRepository extends JpaRepository<Team, UUID> {
  List<Team> findByHackathonEventId(UUID eventId);
}
