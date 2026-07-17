package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.openapi.model.RoleDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.StudentStatusDto;

@Entity
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class Student extends User {
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

  @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Team> teams = new ArrayList<>();

  @Column(columnDefinition = "TEXT", nullable = false)
  @Nonnull
  private String schoolName;

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

  // Returns whether this Student is the leader of a specific team.
  public boolean isTeamLeaderOf(Team team) {
    return team.getLeader().equals(this);
  }

  public StudentDto toDto() {
    return new StudentDto(
        getId(),
        getEmail(),
        RoleDto.STUDENT,
        getFullName(),
        getStudentId(),
        isExternal(),
        getSchoolName(),
        getTeams().stream().map(team -> team.getId()).toArray(UUID[]::new),
        StudentStatusDto.fromValue(getStudentStatus().name()),
        getEvents().stream().map(event -> event.getId()).toArray(UUID[]::new));
  }
}
