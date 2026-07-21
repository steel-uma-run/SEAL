package seal.backend.entities.notification;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Notif;
import seal.backend.entities.Submission;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class RegradeNotif extends Notif {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false)
  @Nonnull
  private Submission submission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecturer_id", nullable = false)
  @Nonnull
  private Lecturer lecturer;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String reason;

  @Column(nullable = false)
  private boolean isResolved;
}
