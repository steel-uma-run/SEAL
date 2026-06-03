package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.util.UUID;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String fullName;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Enumerated(EnumType.STRING)
  private StudentType studentType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String email;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  private StudentStatus studentStatus;

  public User() {}

  public User(String fullName, String email, String passwordHash) {
    this.fullName = fullName;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public java.util.UUID getId() {
    return id;
  }

  public void setId(java.util.UUID id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public StudentType getStudentType() {
    return studentType;
  }

  public void setStudentType(StudentType studentType) {
    this.studentType = studentType;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public StudentStatus getStudentStatus() {
    return studentStatus;
  }

  public void setStudentStatus(StudentStatus studentStatus) {
    this.studentStatus = studentStatus;
  }
}
