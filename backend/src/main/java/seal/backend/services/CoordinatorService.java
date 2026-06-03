package seal.backend.services;

import java.util.List;
import seal.backend.entities.Student;

public interface CoordinatorService {
  List<Student> getUnapprovedStudents();
}
