package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@Getter
@Setter
@Table(name = "students")
public class Student extends User {
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudentType studentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StudentStatus studentStatus;

  @Column(nullable = false)
  private String studentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = true)
  private Team team;

  public Student() {}

  public Student(User user, String studentId, StudentType studentType) {
    super();

    this.setFullName(user.getFullName());
    this.setEmail(user.getEmail());
    this.setPasswordHash(user.getPasswordHash());

    this.studentType = studentType;
    this.studentStatus = StudentStatus.ACTIVE; // TODO: TEMPORARY until coord can approve
    this.studentId = studentId;

    setRole(Role.STUDENT);
  }
}
