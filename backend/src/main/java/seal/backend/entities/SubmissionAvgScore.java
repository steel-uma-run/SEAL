package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import seal.openapi.model.SubmissionAvgScoreDto;

@Entity
@Table(
    name = "submission_avg_scores",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_submission_judge",
            columnNames = {"submission_id", "lecturer_id"}))
@NoArgsConstructor
@Getter
@Setter
public class SubmissionAvgScore {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @ManyToOne(optional = false)
  @JoinColumn(name = "lecturer_id", nullable = false)
  private Lecturer lecturer;

  // judge weighted total (0..100)
  private double avgScore;

  public SubmissionAvgScoreDto toDto() {
    return new SubmissionAvgScoreDto(lecturer.getId(), (float) avgScore);
  }
}
