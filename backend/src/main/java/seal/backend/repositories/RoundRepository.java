package seal.backend.repositories;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Round;

public interface RoundRepository extends JpaRepository<Round, UUID> {
  boolean existsByEventIdAndStartTimeLessThanAndEndTimeGreaterThan(
      UUID eventId, OffsetDateTime startTime, OffsetDateTime endTime);
}
