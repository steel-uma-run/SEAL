package seal.backend.repositories;

import java.util.List;
import seal.backend.entities.Student;
import seal.backend.enums.StudentStatus;

public interface StudentRepository extends UserRepository {
  List<Student> findByStudentStatus(StudentStatus studentStatus);
}
