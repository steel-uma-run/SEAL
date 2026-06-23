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
import org.springframework.web.bind.annotation.RestController;
import seal.backend.services.HackathonEventService;
import seal.openapi.api.EventsApi;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;

@RestController
@RequiredArgsConstructor
public class HackathonEventController implements EventsApi {
  private final HackathonEventService eventService;

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<HackathonEventDto> createEvent(
      @Valid @RequestBody CreateEventRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
  }

  @Override
  public ResponseEntity<HackathonEventDto[]> getEventsInSeason(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId) {
    return ResponseEntity.ok(eventService.getAllEvents().toArray(HackathonEventDto[]::new));
  }

  @Override
  public ResponseEntity<HackathonEventDto> getEvent(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId,
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    return ResponseEntity.ok(eventService.getEvent(seasonId, eventId));
  }
}
