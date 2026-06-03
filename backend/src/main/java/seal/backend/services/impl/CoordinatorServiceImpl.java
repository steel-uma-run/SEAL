package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.Student;
import seal.backend.enums.StudentStatus;
import seal.backend.repositories.StudentRepository;
import seal.backend.services.CoordinatorService;

@Service
@RequiredArgsConstructor
public class CoordinatorServiceImpl implements CoordinatorService {
  private final StudentRepository studentRepository;

  @Override
  public List<Student> getUnapprovedStudents() {
    return studentRepository.findByStudentStatus(StudentStatus.PENDING);
  }
}
