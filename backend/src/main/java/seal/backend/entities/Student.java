package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@Table(name = "students")
public class Student extends User {
  @Enumerated(EnumType.ORDINAL)
  @Column(nullable = false)
  private StudentType studentType;

  @Enumerated(EnumType.ORDINAL)
  @Column(nullable = false)
  private StudentStatus studentStatus;

  @Column(nullable = false)
  private String studentId;

  public Student() {}

  public Student(User user, String studentId, StudentType studentType) {
    super(user.getFullName(), user.getEmail(), user.getPasswordHash());

    this.studentType = studentType;
    this.studentStatus = StudentStatus.PENDING;
    this.studentId = studentId;

    setRole(Role.STUDENT);
  }

  public StudentType getStudentType() {
    return studentType;
  }

  public void setStudentType(StudentType studentType) {
    this.studentType = studentType;
  }

  public StudentStatus getStudentStatus() {
    return studentStatus;
  }

  public void setStudentStatus(StudentStatus studentStatus) {
    this.studentStatus = studentStatus;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }
}
