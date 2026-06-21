package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TrackDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class TrackController implements TracksApi {
  private final TrackService trackService;

  @Override
  public ResponseEntity<TrackDto[]> getAllTracksOfEvent(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    List<TrackDto> dtos = trackService.getAllTracksOfEvent(eventId);
    return ResponseEntity.ok(dtos.toArray(TrackDto[]::new));
  }

  @Override
  public ResponseEntity<TrackDto> getTrack(@PathVariable(name = "trackId") @NotNull UUID trackId) {
    TrackDto trackDto = trackService.getTrack(trackId);
    return ResponseEntity.ok(trackDto);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> createTrack(
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull CreateTrackRequestDto request) {
    TrackDto responseDto = trackService.createTrack(request, eventId);
    return ResponseEntity.ok(responseDto);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> assignMentor(
      @PathVariable(name = "trackId") @NotNull UUID trackId,
      @RequestBody @Valid @NotNull seal.openapi.model.AssignMentorRequestDto request) {
    TrackDto updatedTrack = trackService.assignMentor(trackId, request);
    return ResponseEntity.ok(updatedTrack);
  }
}
