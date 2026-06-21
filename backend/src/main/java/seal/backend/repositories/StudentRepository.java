package seal.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.StudentStatus;

public interface StudentRepository extends JpaRepository<Student, UUID> {
  Optional<Student> findByUser(User user);

  Optional<Student> findByUserEmail(String email);

  List<Student> findByStudentStatus(StudentStatus studentStatus);

  boolean existsByStudentId(String studentId);
}
