package seal.backend.services.impl;

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
import seal.backend.requests.RegisterRequest;
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

    User newUser =
        new User(
            registerRequest.name(),
            registerRequest.email(),
            passwordEncoder.encode(registerRequest.password()));
    Student newStudent =
        new Student(
            newUser,
            registerRequest.studentId(),
            registerRequest.isExternal() ? StudentType.EXTERNAL : StudentType.FPT);

    studentRepository.save(newStudent);
  }

  @Override
  public String login(String email, String password) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(email, password);
    Authentication auth = authenticationManager.authenticate(token);

    return JwtService.sign(email);
  }

  @Override
  public User getCurrentUser(String email) throws UsernameNotFoundException {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
  }
}
