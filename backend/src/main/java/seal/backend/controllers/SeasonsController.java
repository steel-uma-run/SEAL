package seal.backend.controllers;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.SeasonsService;
import seal.openapi.api.SeasonsApi;
import seal.openapi.model.SeasonDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class SeasonsController implements SeasonsApi {
  private final SeasonsService seasonsService;

  @Override
  public ResponseEntity<SeasonDto[]> getAllSeasons() {
    return ResponseEntity.ok(seasonsService.getAllSeasons());
  }

  @Override
  public ResponseEntity<SeasonDto> getSeason(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId) {
    return ResponseEntity.ok(seasonsService.getSeason(seasonId));
  }
}
