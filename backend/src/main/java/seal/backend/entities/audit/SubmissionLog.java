package seal.backend.entities.audit;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Student;
import seal.backend.entities.Submission;
import seal.openapi.model.SubmissionLogDto;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
public class SubmissionLog extends AuditLog {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", nullable = false)
  @Nonnull
  private Student actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false)
  @Nonnull
  private Submission submission;

  @Override
  public SubmissionLogDto toDto() {
    return new SubmissionLogDto(
        getId(), getActionTime(), getActor().getEmail(), getSubmission().getId());
  }
}
