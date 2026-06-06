package seal.backend.repositories;

import java.util.List;
import java.util.Optional;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.StudentStatus;

public interface StudentRepository extends UserRepository {
  List<Student> findByStudentStatus(StudentStatus studentStatus);

  Optional<User> findByStudentId(String studentId);
}
