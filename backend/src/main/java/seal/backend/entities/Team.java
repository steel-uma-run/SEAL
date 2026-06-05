package seal.backend.entities;

import jakarta.persistence.*;
import java.util.UUID;
import seal.backend.enums.TeamStatus;

@Entity
@Table(name = "teams")
public class Team {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String name;

  @Column(nullable = true, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  private TeamStatus teamStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "season_id", nullable = false)
  private Season season;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leader_id", nullable = false)
  private Student leader;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mentor_id", nullable = true) // tam thoi de null vi chua duoc phan cong nhe
  private Lecturer mentor;

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TeamStatus getTeamStatus() {
    return teamStatus;
  }

  public void setTeamStatus(TeamStatus teamStatus) {
    this.teamStatus = teamStatus;
  }

  public Season getSeason() {
    return season;
  }

  public void setSeason(Season season) {
    this.season = season;
  }

  public Student getLeader() {
    return leader;
  }

  public void setLeader(Student leader) {
    this.leader = leader;
  }

  public Lecturer getMentor() {
    return mentor;
  }

  public void setMentor(Lecturer mentor) {
    this.mentor = mentor;
  }
}
