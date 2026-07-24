package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.SubmissionService;
import seal.backend.services.TeamService;
import seal.openapi.api.TeamsApi;
import seal.openapi.model.CreateTeamRequestPayloadDto;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.TeamDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class TeamController implements TeamsApi {
  private final TeamService teamService;
  private final SubmissionService submissionService;

  @Override
  @PreAuthorize("hasAnyAuthority('STUDENT', 'COORDINATOR')")
  public ResponseEntity<TeamDto> createTeam(
      @RequestBody @Valid @NotNull CreateTeamRequestPayloadDto request) {
    TeamDto responseDto = teamService.createTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @Override
  @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER')")
  public ResponseEntity<SubmissionDto[]> getAllSubmissions(
      @PathVariable(name = "teamId") @NotNull UUID teamId) {
    return ResponseEntity.ok(
        submissionService.getAllSubmissions(teamId).toArray(SubmissionDto[]::new));
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> approveTeam(@PathVariable(name = "teamId") @NotNull UUID teamId) {
    teamService.approveTeam(teamId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<Void> inviteToTeam(
      @PathVariable(name = "teamId") @NotNull UUID teamId,
      @PathVariable(name = "studentId") @NotNull UUID studentId) {
    teamService.inviteToTeam(teamId, studentId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAnyAuthority('STUDENT', 'COORDINATOR', 'LECTURER')")
  public ResponseEntity<TeamDto> getTeamInfo(@PathVariable(name = "teamId") @NotNull UUID teamId) {
    return ResponseEntity.ok(teamService.getTeamInfo(teamId));
  }
}
