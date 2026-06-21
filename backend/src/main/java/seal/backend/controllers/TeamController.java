package seal.backend.controllers;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.TeamService;
import seal.openapi.api.TeamsApi;
import seal.openapi.model.CreateTeamRequestDto;
import seal.openapi.model.TeamDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class TeamController implements TeamsApi {
  private final TeamService teamService;

  @Override
  @PreAuthorize("hasAnyAuthority('STUDENT', 'COORDINATOR')")
  public ResponseEntity<TeamDto> createTeam(CreateTeamRequestDto request) {
    TeamDto responseDto = teamService.createTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @Override
  public ResponseEntity<TeamDto[]> getAllTeamsOfEvents(UUID seasonId, UUID eventId) {
    List<TeamDto> dtos = teamService.getAllTeamsOfEvent(eventId);

    return ResponseEntity.ok(dtos.toArray(TeamDto[]::new));
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> approveTeam(@PathVariable(name = "teamId") @NotNull UUID teamId) {
    teamService.approveTeam(teamId);
    return ResponseEntity.ok().build();
  }
}
