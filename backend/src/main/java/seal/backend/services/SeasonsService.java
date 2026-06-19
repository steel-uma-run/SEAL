package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.CreateSeasonRequestDto;
import seal.openapi.model.SeasonDto;

public interface SeasonsService {
  List<SeasonDto> getAllSeasons();

  SeasonDto getSeason(UUID seasonId);

  SeasonDto createSeason(CreateSeasonRequestDto request);
}
