package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.CriteriaDto;
import seal.openapi.model.RoundDto;

@Entity
@Table(name = "rounds")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Round {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @Column(nullable = false)
  @Nonnull
  private Duration activeDuration;

  @Column(nullable = false)
  @Nonnull
  private Duration gradingDuration;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  @Nonnull
  private HackathonEvent event;

  @Column(columnDefinition = "timestamptz")
  private OffsetDateTime activeTime;

  @Column(columnDefinition = "timestamptz")
  private OffsetDateTime gradingStartTime;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "round_criteria",
      joinColumns = @JoinColumn(name = "round_id", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "criteria_id", nullable = false))
  private List<Criteria> criteria = new ArrayList<>();

  public RoundDto toDto() {
    return new RoundDto(
        getId(),
        getName(),
        getDescription(),
        getActiveTime(),
        getGradingStartTime(),
        getActiveDuration().toMillis(),
        getGradingDuration().toMillis(),
        getCriteria().stream().map(Criteria::toDto).toArray(CriteriaDto[]::new));
  }
}
