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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.openapi.model.TrackDto;

@Entity
@Table(name = "tracks")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Track {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  @Nonnull
  private HackathonEvent event;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "track_mentors",
      joinColumns = @JoinColumn(name = "track_id"),
      inverseJoinColumns = @JoinColumn(name = "lecturer_id"))
  private List<Lecturer> mentors = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Lecturer> judges = new ArrayList<>();

  public TrackDto toDto() {
    return new TrackDto(
        getId(),
        getName(),
        getDescription(),
        getEvent().getId(),
        getMentors().stream().map(Lecturer::getId).toArray(UUID[]::new),
        getJudges().stream().map(Lecturer::getId).toArray(UUID[]::new));
  }
}
