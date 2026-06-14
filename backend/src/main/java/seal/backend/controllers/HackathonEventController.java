package seal.backend.controllers;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.entities.HackathonEvent;
import seal.backend.requests.CreateEventRequest;
import seal.backend.services.HackathonEventService;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class HackathonEventController {
  private final HackathonEventService eventService;

  @PreAuthorize("hasAuthority('COORDINATOR')")
  @PostMapping(value = {"", "/"})
  public ResponseEntity<HackathonEvent> createEvent(
      @Valid @RequestBody CreateEventRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
  }

  @GetMapping
  public ResponseEntity<List<HackathonEvent>> getAllEvents() {
    return ResponseEntity.ok(eventService.getAllEvents());
  }
}
