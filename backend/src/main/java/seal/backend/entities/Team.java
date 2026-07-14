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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.TeamStatus;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TeamStatusDto;

@Entity
@Table(name = "teams")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Team {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  @Nonnull
  private String description;

  @Enumerated(EnumType.STRING)
  @Nonnull
  private TeamStatus teamStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  @Nonnull
  private HackathonEvent hackathonEvent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id", nullable = false)
  @Nonnull
  private Student leader;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "track_id", nullable = true)
  private Track track;

  @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
  private List<Student> members = new ArrayList<>();

  // Returns whether the team is eligible to participate in an event.
  public boolean isTeamValid() {
    return teamStatus == TeamStatus.ACTIVE && members.size() >= 3 && members.size() <= 5;
  }

  public TeamDto toDto() {
    return new TeamDto(
        getId(),
        getName(),
        TeamStatusDto.valueOf(getTeamStatus().name()),
        getMembers().stream().map(Student::getId).toArray(UUID[]::new),
        getLeader().getId(),
        getTrack() != null ? getTrack().getId() : null);
  }
}
