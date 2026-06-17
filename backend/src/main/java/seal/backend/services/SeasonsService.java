package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.backend.entities.Season;

public interface SeasonsService {
  List<Season> getAllSeasons();

  Season getSeason(UUID seasonId);

  Season createSeason(Season newSeason);
}
