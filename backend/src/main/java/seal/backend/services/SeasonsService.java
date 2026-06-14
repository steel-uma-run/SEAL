package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.SeasonDto;

public interface SeasonsService {
  public SeasonDto[] getAllSeasons();

  public SeasonDto getSeason(UUID seasonId);
}
