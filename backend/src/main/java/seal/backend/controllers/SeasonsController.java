package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.config.GlobalConfig;
import seal.backend.entities.Season;
import seal.backend.mapper.SeasonMapper;
import seal.backend.services.SeasonsService;
import seal.openapi.api.SeasonsApi;
import seal.openapi.model.CreateSeasonRequestDto;
import seal.openapi.model.SeasonDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class SeasonsController implements SeasonsApi {
  private final SeasonsService seasonsService;
  private final SeasonMapper seasonMapper;

  @Override
  public ResponseEntity<SeasonDto[]> getAllSeasons() {
    // Lấy list Entity từ DB
    List<Season> seasons = seasonsService.getAllSeasons();

    // Dùng stream() kết hợp MapStruct để dịch toàn bộ list sang DTO Array
    SeasonDto[] dtos = seasons.stream().map(seasonMapper::toDto).toArray(SeasonDto[]::new);

    return ResponseEntity.ok(dtos);
  }

  @Override
  public ResponseEntity<SeasonDto> getSeason(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId) {

    Season season = seasonsService.getSeason(seasonId);
    return ResponseEntity.ok(seasonMapper.toDto(season));
  }

  @Override
  public ResponseEntity<SeasonDto> createSeason(
      @RequestBody @Valid @NotNull CreateSeasonRequestDto request) {

    Season entityToSave = seasonMapper.toEntity(request);

    Season savedEntity = seasonsService.createSeason(entityToSave);

    return ResponseEntity.status(HttpStatus.CREATED).body(seasonMapper.toDto(savedEntity));
  }

  // TODO: implement
  @Override
  public ResponseEntity<SeasonDto> createSeason(
      @RequestBody @Valid @NotNull CreateSeasonRequestDto body) {
    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
  }
}
