package seal.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.ProfileService;
import seal.openapi.api.ProfileApi;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class ProfileController implements ProfileApi {
  private final ProfileService profileService;

  @Override
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Object> getSelfProfile() {
    return ResponseEntity.ok(profileService.getSelfProfile());
  }
}
