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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.ScoreDto;
import seal.openapi.model.SubmissionDto;

@Entity
@Table(name = "submissions")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime submitTime;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String githubLink;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String ytLink;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String slideLink;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  @Nonnull
  private Team submitterTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "round_id", nullable = false)
  @Nonnull
  private Round round;

  @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
  private List<Score> scores = new ArrayList<>();

  @Column(name = "avg_score")
  private Float avgScore;

  public Double calculateTotalScore(Lecturer lecturer) {
    if (scores == null || scores.isEmpty()) {
      return null;
    }

    return scores.stream()
        .filter(s -> s.getLecturer().getId().equals(lecturer.getId()))
        .mapToDouble(s -> (s.getValue() * s.getCriteria().getWeight()) / 10.0)
        .sum();
  }

  public Float calculateAverageScore() {
    if (scores == null || scores.isEmpty()) {
      return null;
    }

    var lecturerIds = scores.stream().map(s -> s.getLecturer().getId()).distinct().toList();

    if (lecturerIds.isEmpty()) {
      return null;
    }

    double avg =
        lecturerIds.stream()
            .mapToDouble(
                id ->
                    scores.stream()
                        .filter(s -> s.getLecturer().getId().equals(id))
                        .mapToDouble(s -> (s.getValue() * s.getCriteria().getWeight()) / 10.0)
                        .sum())
            .average()
            .orElse(0.0);

    return (float) avg;
  }

  public void refreshAvgScore() {
    this.avgScore = calculateAverageScore();
  }

  public SubmissionDto toDto() {
    return new SubmissionDto(
        getId(),
        getTitle(),
        getDescription(),
        getGithubLink(),
        getYtLink(),
        getSlideLink(),
        getSubmitTime(),
        scores.stream().map(Score::toDto).toArray(ScoreDto[]::new),
        avgScore);
  }
}
