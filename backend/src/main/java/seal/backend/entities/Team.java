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
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.TeamStatus;

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
  @JoinColumn(name = "season_id", nullable = false)
  @Nonnull
  private HackathonEvent hackathonEvent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id", nullable = false)
  @Nonnull
  private Student leader;

  // team can have no track if coord did not assign
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "track_id", nullable = true)
  private Track track;

  // tam thoi de null vi chua duoc phan cong nhe
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mentor_id", nullable = true)
  private Lecturer mentor;
}
