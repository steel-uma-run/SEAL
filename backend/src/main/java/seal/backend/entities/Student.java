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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@NoArgsConstructor
@SuperBuilder
@Getter
public class Student extends User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @Nonnull
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private StudentType studentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private StudentStatus studentStatus;

  @Column(nullable = false, unique = true)
  @Nonnull
  private String studentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = true)
  private Team team;

  // TODO
  // Add schoolName

  @ManyToMany
  @JoinTable(
      name = "events_interest",
      joinColumns = @JoinColumn(name = "students_id"),
      inverseJoinColumns = @JoinColumn(name = "events_id"))
  @Builder.Default
  private Set<HackathonEvent> events = new HashSet<>();

  public boolean isExternal() {
    return studentType != StudentType.FPT;
  }

  // Returns whether this Student is the leader of their team.
  public boolean isTeamLeader() {
    return team != null && isTeamLeaderOf(team);
  }

  // Returns whether this Student is the leader of a specific team.
  public boolean isTeamLeaderOf(Team team) {
    return team.getLeader().getId().equals(id);
  }
}
