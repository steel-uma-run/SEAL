package seal.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;

@Entity
@Table(name = "students")
public class Student extends User {
  @Column(nullable = false)
  private StudentType studentType;

  @Column(nullable = false)
  private StudentStatus studentStatus;

  public Student() {}

  public Student(User user, StudentType studentType) {
    super(user.getFullName(), user.getEmail(), user.getPasswordHash());
    this.studentType = studentType;
    this.studentStatus = StudentStatus.PENDING;
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
}
