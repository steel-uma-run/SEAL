package seal.backend.controllers;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.entities.HackathonEvent;
import seal.backend.requests.CreateSeasonRequest;
import seal.backend.services.SeasonsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seasons")
public class SeasonsController {
  private final SeasonsService seasonsService;

  @GetMapping(value = {"", "/"})
  public ResponseEntity<List<HackathonEvent>> getAll() {
    return ResponseEntity.ok(seasonsService.getAll());
  }

  @PostMapping(value = {"", "/"})
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<?> create(@Valid @RequestBody CreateSeasonRequest request) {
    return ResponseEntity.ok(seasonsService.create(request));
  }
}
