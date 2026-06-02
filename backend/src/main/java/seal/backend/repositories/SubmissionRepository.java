package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {}
