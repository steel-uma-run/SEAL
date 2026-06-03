package seal.backend.controllers;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.exceptions.StudentNotPendingException;
import seal.backend.exceptions.UserNotStudentException;
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

  @PreAuthorize("hasAuthority('COORDINATOR')")
  @PostMapping("/{id}/approve")
  public ResponseEntity<?> approve(@PathVariable UUID id) {
    try {
      profileService.approve(id);
      return ResponseEntity.ok().build();
    } catch (UsernameNotFoundException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
    } catch (UserNotStudentException | StudentNotPendingException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }
}
