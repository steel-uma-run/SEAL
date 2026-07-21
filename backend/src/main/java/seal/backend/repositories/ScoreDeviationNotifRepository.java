package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.notification.ScoreDeviationNotif;

public interface ScoreDeviationNotifRepository extends JpaRepository<ScoreDeviationNotif, UUID> {
  List<ScoreDeviationNotif> findBySubmissionId(UUID submissionId);
}
