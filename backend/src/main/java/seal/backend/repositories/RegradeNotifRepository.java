package seal.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.notification.RegradeNotif;

public interface RegradeNotifRepository extends JpaRepository<RegradeNotif, UUID> {
  Optional<RegradeNotif> findBySubmissionIdAndLecturerIdAndIsResolvedFalse(
      UUID submissionId, UUID lecturerId);

  List<RegradeNotif> findBySubmissionIdAndIsResolvedFalse(UUID submissionId);
}
