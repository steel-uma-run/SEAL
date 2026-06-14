package seal.backend.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Student;
import seal.backend.enums.StudentStatus;
import seal.backend.repositories.StudentRepository;
import seal.backend.services.AccountService;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final StudentRepository studentRepository;

  @Override
  public void approve(UUID id) {
    Student student =
        studentRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "This account does not exist."));

    if (student.getStudentStatus() != StudentStatus.PENDING) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Student is not in PENDING status.");
    }

    student.setStudentStatus(StudentStatus.ACTIVE);
  }
}
