package seal.backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.entities.Season;
import seal.backend.services.SeasonsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seasons")
public class SeasonsController {
  private final SeasonsService seasonsService;

  @GetMapping(value = {"", "/"})
  public ResponseEntity<List<Season>> getAll() {
    return ResponseEntity.ok(seasonsService.getAll());
  }
}
