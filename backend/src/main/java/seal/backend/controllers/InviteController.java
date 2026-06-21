package seal.backend.controllers;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.InviteService;
import seal.openapi.api.InvitesApi;
import seal.openapi.model.TeamInviteDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class InviteController implements InvitesApi {
  private final InviteService inviteService;

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<TeamInviteDto[]> getAllInvites() {
    return ResponseEntity.ok(inviteService.getAllInvites().toArray(TeamInviteDto[]::new));
  }

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<Void> acceptInvite(
      @PathVariable(name = "inviteId") @NotNull UUID inviteId) {
    inviteService.acceptInvite(inviteId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<Void> declineInvite(
      @PathVariable(name = "inviteId") @NotNull UUID inviteId) {
    inviteService.declineInvite(inviteId);
    return ResponseEntity.ok().build();
  }
}
