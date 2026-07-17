package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Score;

public interface ScoreRepository extends JpaRepository<Score, UUID> {}
