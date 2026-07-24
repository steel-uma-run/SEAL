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
import java.time.Duration;
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

  @Column(nullable = false)
  @Nonnull
  private Duration registrationDuration;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private EventStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_id", nullable = false)
  @Nonnull
  private Season season;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String prize;

  @Column(columnDefinition = "timestamptz", nullable = true)
  private OffsetDateTime registrationStartTime;

  @Column(nullable = true)
  private int teamsLimit;

  @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
  private Set<Student> students = new HashSet<>();

  @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
  private List<Round> rounds = new ArrayList<>();

  @OneToMany(mappedBy = "hackathonEvent", fetch = FetchType.LAZY)
  private List<Team> teams = new ArrayList<>();

  // Returns the currently active round. According to business rules/constraints an event can only
  // have 1 active round at any given time.
  //
  // "Active round" is the round that has been started most recently.
  public Optional<Round> getActiveRound() {
    return rounds.stream()
        .filter(round -> round.getGradingStartTime() != null)
        .sorted((a, b) -> b.getGradingStartTime().compareTo(a.getGradingStartTime()))
        .findFirst();
  }

  // Returns if event has started at some point
  public boolean hasStarted() {
    return rounds.stream().filter(round -> round.getActiveTime() != null).findAny().isPresent();
  }

  public HackathonEventDto toDto() {
    return new HackathonEventDto(
        getId(),
        getName(),
        getDescription(),
        HackathonEventStatusDto.fromValue(getStatus().name()),
        getRegistrationStartTime(),
        getRegistrationDuration().toMillis(),
        getSeason().getId(),
        getPrize(),
        getRounds().stream().map(Round::toDto).toArray(RoundDto[]::new));
  }
}
