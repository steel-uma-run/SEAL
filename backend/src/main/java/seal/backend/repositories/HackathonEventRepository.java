package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.HackathonEvent;
import seal.backend.enums.EventStatus;

public interface HackathonEventRepository extends JpaRepository<HackathonEvent, UUID> {
  List<HackathonEvent> findByStatus(EventStatus status);
}
