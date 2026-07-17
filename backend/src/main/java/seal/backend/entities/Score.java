package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.ScoreDto;

@Entity
@Table(name = "scores")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Score {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "criteria_id", nullable = false)
  @Nonnull
  private Criteria criteria;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submission_id", nullable = false)
  @Nonnull
  private Submission submission;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecturer_id", nullable = false)
  @Nonnull
  private Lecturer lecturer;

  // Stored as integer; enforce 0..100 at DB level via CHECK constraint if you want.
  @Column(name = "value", nullable = false)
  @Nonnull
  private Integer value;

  @Column(columnDefinition = "TEXT")
  private String comment;

  public ScoreDto toDto() {
    return new ScoreDto(id, value, lecturer.getId(), criteria.getId(), submission.getId(), comment);
  }
}
