package seal.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.entities.Team;
import seal.backend.requests.CreateTeamRequest;
import seal.backend.services.TeamService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {
  private final TeamService teamService;

  @PreAuthorize("hasAnyAuthority('STUDENT', 'COORDINATOR')")
  @PostMapping(value = {"", "/"})
  public ResponseEntity<Team> createTeam(@RequestBody CreateTeamRequest request) {
    Team newTeam = teamService.createTeam(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(newTeam);
  }
}
