package seal.backend.services.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.AccountService;
import seal.openapi.model.CreateLecturerRequestDto;
import seal.openapi.model.StudentDto;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final UserRepository userRepo;
  private final StudentRepository studentRepository;
  private final LecturerRepository lecturerRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Object[] getAllAccounts() {
    List<User> users = userRepo.findAll();
    return users.stream().map(user -> user.toDto()).toArray();
  }

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
    studentRepository.save(student);
  }

  @Override
  public StudentDto[] getUnapprovedStudents() {
    List<Student> unapproved = studentRepository.findByStudentStatus(StudentStatus.PENDING);
    return unapproved.stream().map(student -> student.toDto()).toArray(StudentDto[]::new);
  }

  @Override
  public Object createLecturer(CreateLecturerRequestDto request) {
    if (userRepo.findByEmail(request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
    }

    Lecturer lecturer =
        Lecturer.builder()
            .email(request.email())
            .fullName(request.name())
            .role(Role.LECTURER)
            .passwordHash(passwordEncoder.encode("admin"))
            .build();

    lecturerRepository.save(lecturer);
    return lecturer.toDto();
  }
}
