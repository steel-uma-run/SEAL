package seal.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import seal.backend.responses.ProfileResponse;
import seal.backend.services.ProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {
  private final ProfileService profileService;

  @GetMapping(value = {"", "/"})
  public ResponseEntity<?> get(Authentication auth) {
    String email = auth.getName();
    ProfileResponse profile = profileService.get(email);

    return ResponseEntity.ok(profile);
  }
}
