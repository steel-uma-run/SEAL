package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Round;

public interface RoundRepository extends JpaRepository<Round, UUID> {}
