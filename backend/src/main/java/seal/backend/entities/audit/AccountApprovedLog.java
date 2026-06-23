package seal.backend.entities.audit;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Coordinator;
import seal.backend.entities.Student;

@Entity
@SuperBuilder
@NoArgsConstructor
public class AccountApprovedLog extends AuditLog {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", nullable = false)
  @Nonnull
  private Coordinator actor;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_student_id", nullable = false)
  @Nonnull
  private Student approvedStudent;
}
