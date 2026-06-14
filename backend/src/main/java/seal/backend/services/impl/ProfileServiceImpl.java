package seal.backend.services.impl;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.exceptions.StudentNotPendingException;
import seal.backend.exceptions.UserNotStudentException;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.responses.ProfileResponse;
import seal.backend.services.ProfileService;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;

  public ProfileResponse get(String email) {
    User user = userRepository.findByEmail(email).get();
    ProfileResponse profile =
        new ProfileResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());

    return profile;
  }

  public void approve(UUID id)
      throws UsernameNotFoundException, UserNotStudentException, StudentNotPendingException {
    Optional<User> maybeUser = userRepository.findById(id);

    if (maybeUser.isEmpty()) {
      throw new UsernameNotFoundException("User with ID not found.");
    }

    User user = maybeUser.get();
    if (user.getRole() != Role.STUDENT) {
      throw new UserNotStudentException("This user is not a student.");
    }

    Student student = studentRepository.findByUserId(user.getId()).get();
    if (student.getStudentStatus() != StudentStatus.PENDING) {
      throw new StudentNotPendingException("This student is not pending approval.");
    }

    student.setStudentStatus(StudentStatus.ACTIVE);
    studentRepository.save(student);
  }
}
