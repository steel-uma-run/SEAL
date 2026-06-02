package seal.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.JwtService;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.enums.StudentType;
import seal.backend.repositories.StudentRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired private StudentRepository studentRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private AuthenticationManager authenticationManager;

  @PostMapping("/register")
  public ResponseEntity<Void> register(
      @RequestParam("email") String email,
      @RequestParam("name") String name,
      @RequestParam("password") String password,
      @RequestParam("external") boolean external) {
    if (studentRepository.findByEmail(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
    }

    User newUser = new User(name, email, passwordEncoder.encode(password));
    Student newStudent = new Student(newUser, external ? StudentType.EXTERNAL : StudentType.FPT);

    studentRepository.save(newStudent);

    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam("email") String email, @RequestParam("password") String password) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(email, password);
    Authentication auth = authenticationManager.authenticate(token);

    return ResponseEntity.ok(JwtService.sign(email));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication auth) {
    UserDetails principal = (UserDetails) auth.getPrincipal();
    User user = studentRepository.findByEmail(principal.getUsername()).get();

    return ResponseEntity.ok(user);
  }
}
