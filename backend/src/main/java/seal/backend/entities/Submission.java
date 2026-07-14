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
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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

  public SubmissionDto toDto() {
    return new SubmissionDto(
        getTitle(),
        getDescription(),
        getGithubLink(),
        getYtLink(),
        getSlideLink(),
        getSubmitTime());
  }
}
