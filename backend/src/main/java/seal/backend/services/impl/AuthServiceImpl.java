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
import seal.backend.exceptions.EmailExistsException;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.requests.LoginRequest;
import seal.backend.requests.RegisterRequest;
import seal.backend.responses.LoginResponse;
import seal.backend.services.AuthService;
import seal.backend.services.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Override
  public void register(RegisterRequest registerRequest) throws EmailExistsException {
    if (studentRepository.findByEmail(registerRequest.email()).isPresent()) {
      throw new EmailExistsException("This email is already registered.");
    }

    User newUser = new User();
    newUser.setFullName(registerRequest.name());
    newUser.setEmail(registerRequest.email());
    newUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));

    Student newStudent =
        new Student(
            newUser,
            registerRequest.studentId(),
            registerRequest.isExternal() ? StudentType.EXTERNAL : StudentType.FPT);

    studentRepository.save(newStudent);
  }

  @Override
  public LoginResponse login(LoginRequest request) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(request.email(), request.password());
    Authentication auth = authenticationManager.authenticate(token);

    String jwt = JwtService.sign(request.email());
    Optional<User> user = userRepository.findByEmail(request.email());
    LoginResponse resp = new LoginResponse(jwt, user.get());

    return resp;
  }

  @Override
  public User getCurrentUser(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
