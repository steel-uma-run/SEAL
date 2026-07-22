package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
  // TODO: rn we only allow 1 team to participate in 1 event. If we change the relationship to 1
  // team multiple events this will need to be restricted to also query by track id.
  List<Submission> findAllBySubmitterTeamId(UUID teamId);

  List<Submission> findAllBySubmitterTeamHackathonEventId(UUID eventId);
}
