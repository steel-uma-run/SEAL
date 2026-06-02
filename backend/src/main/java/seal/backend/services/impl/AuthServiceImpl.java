package seal.backend.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.StudentType;
import seal.backend.exceptions.EmailExistsException;
import seal.backend.repositories.StudentRepository;
import seal.backend.services.AuthService;
import seal.backend.services.JwtService;

@Component
public class AuthServiceImpl implements AuthService {
  @Autowired private StudentRepository studentRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private AuthenticationManager authenticationManager;

  @Override
  public void register(String email, String name, String password, boolean isExternal)
      throws EmailExistsException {
    if (studentRepository.findByEmail(email).isPresent()) {
      throw new EmailExistsException("This email is already registered.");
    }

    User newUser = new User(name, email, passwordEncoder.encode(password));
    Student newStudent = new Student(newUser, isExternal ? StudentType.EXTERNAL : StudentType.FPT);

    studentRepository.save(newStudent);
  }

  @Override
  public String login(String email, String password) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(email, password);
    Authentication auth = authenticationManager.authenticate(token);

    return JwtService.sign(email);
  }
}
