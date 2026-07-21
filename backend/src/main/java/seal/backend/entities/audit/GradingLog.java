package seal.backend.entities.audit;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Submission;
import seal.backend.entities.User;
import seal.openapi.model.GradingLogDto;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
public class GradingLog extends AuditLog {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", nullable = false)
  @Nonnull
  private User actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false)
  @Nonnull
  private Submission submission;

  @Column(nullable = false)
  @Nonnull
  private String action;

  @Column(columnDefinition = "TEXT")
  private String details;

  @Override
  public GradingLogDto toDto() {
    return new GradingLogDto(
        getId(),
        getActionTime(),
        getActor().getEmail(),
        getSubmission().getId(),
        getAction(),
        getDetails());
  }
}
