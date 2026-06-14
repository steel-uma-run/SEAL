package seal.backend.services.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.services.SeasonsService;
import seal.openapi.model.SeasonDto;
import seal.openapi.model.SeasonSemesterDto;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final SeasonRepository seasonRepository;

  @Override
  public SeasonDto[] getAllSeasons() {
    List<Season> seasonEntities = seasonRepository.findAll();

    return seasonEntities.stream()
        .map(
            entity -> {
              return new SeasonDto(entity.getId(), SeasonSemesterDto.SPRING, 2000);
            })
        .toArray(SeasonDto[]::new);
  }

  @Override
  public SeasonDto getSeason(UUID seasonId) {
    Season season =
        seasonRepository
            .findById(seasonId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season does not exist"));

    // TODO: complete
    return new SeasonDto(season.getId(), SeasonSemesterDto.SPRING, 2026);
  }
}
