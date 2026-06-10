package seal.backend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.services.AuthService;
import seal.openapi.api.AuthApi;
import seal.openapi.model.LoginRequestPayload;
import seal.openapi.model.LoginResponsePayload;
import seal.openapi.model.RegisterRequestPayload;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController implements AuthApi {
  private final AuthService authService;

  @Override
  public ResponseEntity<LoginResponsePayload> login(
      @Valid @RequestBody LoginRequestPayload request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Override
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestPayload request) {
    authService.register(request);
    return ResponseEntity.ok().build();
  }
}
