package seal.backend.entities.audit;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Coordinator;
import seal.backend.entities.Student;

@Entity
@SuperBuilder
@NoArgsConstructor
public class AccountBannedLog extends AuditLog {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", nullable = false)
  @Nonnull
  private Coordinator actor;

  // ManyToOne cuz a student can be banned & unbanned multiple times
  @ManyToOne
  @JoinColumn(name = "ban_target_id", nullable = false)
  @Nonnull
  private Student banTarget;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String banReason;
}
