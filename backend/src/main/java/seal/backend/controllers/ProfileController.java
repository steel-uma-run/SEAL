package seal.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.ProfileService;
import seal.openapi.api.ProfileApi;
import seal.openapi.model.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class ProfileController implements ProfileApi {
  private final ProfileService profileService;

  @Override
  public ResponseEntity<UserDto> getSelfProfile(Authentication auth) {
    return ResponseEntity.ok(profileService.getSelfProfile(auth));
  }

  // @PreAuthorize("hasAuthority('COORDINATOR')")
  // @PostMapping("/{id}/approve")
  // public ResponseEntity<?> approve(@PathVariable UUID id) {
  //   try {
  //     profileService.approve(id);
  //     return ResponseEntity.ok().build();
  //   } catch (UsernameNotFoundException ex) {
  //     throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
  //   } catch (UserNotStudentException | StudentNotPendingException ex) {
  //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
  //   }
  // }
}
