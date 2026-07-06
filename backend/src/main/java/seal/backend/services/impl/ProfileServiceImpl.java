package seal.backend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import seal.backend.entities.User;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.ProfileService;
import seal.backend.enums.Role;
import seal.openapi.model.UserDto;
import seal.openapi.model.UserRoleDto;
import seal.openapi.model.UserStatusDto;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;

  @Override
  public UserDto getSelfProfile(Authentication auth) {
    String email = auth.getName();

    // EXPLANATION: this method should never be invoked by an unauthenticated
    // user. Therefore user should never be null.
    User user = userRepository.findByEmail(email).get();

    UserStatusDto status = null;
    if (user.getRole() == Role.STUDENT) {
      status = studentRepository.findByUser(user)
          .map(student -> UserStatusDto.fromValue(student.getStudentStatus().name()))
          .orElse(null);
    }

    return new UserDto(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        UserRoleDto.fromValue(user.getRole().name()),
        status);
  }

  // public void approve(UUID id)
  //     throws UsernameNotFoundException, UserNotStudentException, StudentNotPendingException {
  //   Optional<User> maybeUser = userRepository.findById(id);

  //   if (maybeUser.isEmpty()) {
  //     throw new UsernameNotFoundException("User with ID not found.");
  //   }

  //   User user = maybeUser.get();
  //   if (user.getRole() != Role.STUDENT) {
  //     throw new UserNotStudentException("This user is not a student.");
  //   }

  //   Student student = studentRepository.findByUserId(user.getId()).get();
  //   if (student.getStudentStatus() != StudentStatus.PENDING) {
  //     throw new StudentNotPendingException("This student is not pending approval.");
  //   }

  //   student.setStudentStatus(StudentStatus.ACTIVE);
  //   studentRepository.save(student);
  // }
}
