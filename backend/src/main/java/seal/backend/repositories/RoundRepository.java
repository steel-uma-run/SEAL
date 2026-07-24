package seal.backend.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Round;
import seal.backend.enums.EventStatus;

public interface RoundRepository extends JpaRepository<Round, UUID> {
  boolean existsByEventIdAndStartTimeLessThanAndEndTimeGreaterThan(
      UUID eventId, OffsetDateTime startTime, OffsetDateTime endTime);

  List<Round> findByEventId(UUID eventId);

  boolean existsByCriteria_CriteriaTemplate_Id(UUID templateId);

  boolean existsByCriteria_CriteriaTemplate_IdAndEvent_StatusNot(
      UUID templateId, EventStatus status);
}
