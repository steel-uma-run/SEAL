package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.SubmissionAvgScore;

public interface SubmissionAvgScoreRepository extends JpaRepository<SubmissionAvgScore, UUID> {
  List<SubmissionAvgScore> findBySubmissionId(UUID submissionId);

  void deleteBySubmissionId(UUID submissionId);

  void deleteBySubmissionIdAndLecturerId(UUID submissionId, UUID lecturerId);
}
