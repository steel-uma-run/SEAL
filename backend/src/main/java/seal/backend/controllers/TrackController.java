package seal.backend.controllers;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;
import seal.backend.services.TrackService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seasons")
class TrackController {
  private final TrackService trackService;

  @PreAuthorize("hasAuthority('COORDINATOR')")
  @PostMapping("/{season_id}/tracks")
  public ResponseEntity<CreateTrackResponse> createTrack(
      @PathVariable("season_id") UUID seasonId, @Valid @RequestBody CreateTrackRequest request) {

    CreateTrackResponse response = trackService.createTrack(seasonId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
