package seal.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.exceptions.EmailExistsException;
import seal.backend.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired private AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<?> register(
      @RequestParam("email") String email,
      @RequestParam("name") String name,
      @RequestParam("password") String password,
      @RequestParam("external") boolean external) {
    try {
      authService.register(email, name, password, external);
      return ResponseEntity.ok().build();
    } catch (EmailExistsException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam("email") String email, @RequestParam("password") String password) {
    return ResponseEntity.ok(authService.login(email, password));
  }
}
