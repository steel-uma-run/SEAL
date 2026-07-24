package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.EventStatus;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.HackathonEventStatusDto;
import seal.openapi.model.RoundDto;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class HackathonEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime startTime;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime endTime;

  @Column(nullable = false)
  private int teamsLimit;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private EventStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_id", nullable = false)
  @Nonnull
  private Season season;

  @Column(columnDefinition = "TEXT")
  @Nonnull
  private String prize;

  @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
  private Set<Student> students = new HashSet<>();

  @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
  private List<Round> rounds = new ArrayList<>();

  @OneToMany(mappedBy = "hackathonEvent", fetch = FetchType.LAZY)
  private List<Team> teams = new ArrayList<>();

  // Returns the currently active round. According to business rules/constraints an event can only
  // have 1 active round at any given time.
  public Optional<Round> getActiveRound() {
    return rounds.stream()
        .filter(
            round -> {
              OffsetDateTime now = OffsetDateTime.now();
              return round.getStartTime().isBefore(now) && round.getEndTime().isAfter(now);
            })
        .findFirst();
  }

  public HackathonEventDto toDto() {
    return new HackathonEventDto(
        getId(),
        getName(),
        getDescription(),
        HackathonEventStatusDto.fromValue(getStatus().name()),
        getStartTime(),
        getEndTime(),
        getSeason().getId(),
        getPrize(),
        getRounds().stream().map(Round::toDto).toArray(RoundDto[]::new));
  }
}
