package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.TrackService;
import seal.openapi.api.TracksApi;
import seal.openapi.model.AssignJudgeRequestDto;
import seal.openapi.model.AssignMentorRequestDto;
import seal.openapi.model.AssignTeamRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateTrackRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class TrackController implements TracksApi {
  private final TrackService trackService;

  @Override
  public ResponseEntity<TrackDto> getTrack(@PathVariable(name = "trackId") @NotNull UUID trackId) {
    TrackDto trackDto = trackService.getTrack(trackId);
    return ResponseEntity.ok(trackDto);
  }

  @Override
  @PreAuthorize("hasAnuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> updateTrack(
      @PathVariable(name = "trackId") @NotNull UUID trackId,
      @RequestBody @Valid @NotNull UpdateTrackRequestDto body) {
    TrackDto updatedTrack = trackService.updateTrack(trackId, body);
    return ResponseEntity.ok(updatedTrack);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> assignMentor(
      @PathVariable(name = "trackId") @NotNull UUID trackId,
      @RequestBody @Valid @NotNull AssignMentorRequestDto body) {

    TrackDto updatedTrack = trackService.assignMentor(trackId, body);
    return ResponseEntity.ok(updatedTrack);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TeamDto> assignTeamToTrack(
      @PathVariable(name = "trackId") @NotNull UUID trackId,
      @RequestBody @Valid @NotNull AssignTeamRequestDto body) {
    TeamDto updatedTeam = trackService.assignTeam(trackId, body);
    return ResponseEntity.ok(updatedTeam);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> assignJudge(
      @PathVariable(name = "trackId") @NotNull UUID trackId,
      @RequestBody @Valid @NotNull AssignJudgeRequestDto body) {
    TrackDto responseDto = trackService.assignJudge(trackId, body);
    return ResponseEntity.ok(responseDto);
  }
}
