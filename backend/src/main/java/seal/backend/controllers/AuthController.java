package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.AuthService;
import seal.openapi.api.AuthApi;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.LoginResponsePayloadDto;
import seal.openapi.model.RegisterRequestPayloadDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AuthController implements AuthApi {
  private final AuthService authService;

  @Override
  public ResponseEntity<LoginResponsePayloadDto> login(
      @Valid @RequestBody @NotNull LoginRequestPayloadDto request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Override
  public ResponseEntity<Void> register(
      @Valid @RequestBody @NotNull RegisterRequestPayloadDto request) {
    authService.register(request);
    return ResponseEntity.ok().build();
  }
}
