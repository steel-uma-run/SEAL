package seal.backend.services.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.StudentType;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.AuthService;
import seal.backend.services.JwtService;
import seal.openapi.model.LoginRequestPayload;
import seal.openapi.model.LoginResponsePayload;
import seal.openapi.model.LoginResponsePayloadUser;
import seal.openapi.model.RegisterRequestPayload;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  public void register(RegisterRequestPayload request) {
    if (studentRepository.findByEmail(request.email()).isPresent()) {
      throw new IllegalArgumentException("This email is already registered.");
    }

    User newUser = new User();
    newUser.setFullName(request.name());
    newUser.setEmail(request.email());
    newUser.setPasswordHash(passwordEncoder.encode(request.password()));

    Student newStudent =
        new Student(
            newUser,
            request.studentId(),
            request.isExternal() ? StudentType.EXTERNAL : StudentType.FPT);

    studentRepository.save(newStudent);
  }

  @Override
  public LoginResponsePayload login(LoginRequestPayload request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.email(), request.password());
    Authentication auth = authenticationManager.authenticate(token);

    String jwt = JwtService.sign(request.email());
    Optional<User> maybeUser = userRepository.findByEmail(request.email());
    if (maybeUser.isEmpty()) {
      throw new IllegalArgumentException("This email does not exist");
    }

    User user = maybeUser.get();

    LoginResponsePayloadUser userDto = new LoginResponsePayloadUser(user.getEmail());
    LoginResponsePayload resp = new LoginResponsePayload(jwt, userDto);

    return resp;
  }

  @Override
  public User getCurrentUser(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
