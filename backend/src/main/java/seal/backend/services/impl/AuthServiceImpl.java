package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.AuthService;
import seal.backend.services.JwtService;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.LoginResponsePayloadDto;
import seal.openapi.model.RegisterRequestPayloadDto;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  @Transactional
  public void register(RegisterRequestPayloadDto request) {
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
    }

    if (studentRepository.existsByStudentId(request.studentId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "This Student ID is already registered.");
    }

    Student newStudent =
        Student.builder()
            .email(request.email())
            .fullName(request.name())
            .role(Role.STUDENT)
            .passwordHash(passwordEncoder.encode(request.password()))
            .schoolName(request.schoolName())
            .studentType(request.isExternal() ? StudentType.EXTERNAL : StudentType.FPT)
            .studentStatus(StudentStatus.PENDING)
            .studentId(request.studentId().toUpperCase())
            .build();

    studentRepository.save(newStudent);
  }

  @Override
  public LoginResponsePayloadDto login(LoginRequestPayloadDto request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.email(), request.password());

    authenticationManager.authenticate(token);

    String jwt = JwtService.sign(request.email());
    Optional<User> maybeUser = userRepository.findByEmail(request.email());
    if (maybeUser.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This email does not exist");
    }

    User user = maybeUser.get();
    return new LoginResponsePayloadDto(jwt, user.toDto());
  }
}
