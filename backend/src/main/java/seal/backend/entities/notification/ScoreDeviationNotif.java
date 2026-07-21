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
import seal.backend.entities.Criteria;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Notif;
import seal.backend.entities.Submission;

@Entity
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ScoreDeviationNotif extends Notif {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false)
  @Nonnull
  private Submission submission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "criteria_id")
  private Criteria criteria;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecturer_id", nullable = false)
  @Nonnull
  private Lecturer lecturer;

  @Column(name = "judge_score", nullable = false)
  private Double judgeScore;

  @Column(name = "average_score", nullable = false)
  private Double averageScore;

  @Column(nullable = false)
  private boolean isResolved;
}
