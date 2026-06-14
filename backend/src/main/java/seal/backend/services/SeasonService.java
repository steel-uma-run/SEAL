package seal.backend.services;

import java.util.List;
import seal.backend.entities.Season;
import seal.backend.requests.CreateSeasonRequest;

public interface SeasonService {
  Season createSeason(CreateSeasonRequest request);

  List<Season> getAllSeasons();
}
