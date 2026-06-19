package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.config.GlobalConfig;
import seal.backend.services.SeasonsService;
import seal.openapi.api.SeasonsApi;
import seal.openapi.model.CreateSeasonRequestDto;
import seal.openapi.model.SeasonDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class SeasonsController implements SeasonsApi {
  private final SeasonsService seasonsService;

  @Override
  public ResponseEntity<SeasonDto[]> getAllSeasons() {
    List<SeasonDto> dtos = seasonsService.getAllSeasons();
    return ResponseEntity.ok(dtos.toArray(SeasonDto[]::new));
  }

  @Override
  public ResponseEntity<SeasonDto> getSeason(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId) {
    SeasonDto seasonDto = seasonsService.getSeason(seasonId);
    return ResponseEntity.ok(seasonDto);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<SeasonDto> createSeason(
      @RequestBody @Valid @NotNull CreateSeasonRequestDto request) {
    SeasonDto responseDto = seasonsService.createSeason(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  // TODO: implement
  @Override
  public ResponseEntity<SeasonDto> createSeason(
      @RequestBody @Valid @NotNull CreateSeasonRequestDto body) {
    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
  }
}
