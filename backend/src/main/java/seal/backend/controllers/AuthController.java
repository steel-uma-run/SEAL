package seal.backend.controllers;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.entities.User;
import seal.backend.exceptions.EmailExistsException;
import seal.backend.requests.RegisterRequest;
import seal.backend.services.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping(value = {"", "/"})
  public ResponseEntity<?> register(@RequestBody RegisterRequest request)
      throws EmailExistsException {
    authService.register(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestParam("email") String email, @RequestParam("password") String password) {
    String token = authService.login(email, password);
    User user = authService.getCurrentUser(email);

    Map<String, Object> response = new HashMap<>();
    response.put("token", token);
    response.put("user", user);

    return ResponseEntity.ok(response);
  }
}
