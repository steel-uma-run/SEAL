package seal.backend.services;

import java.util.List;
import seal.backend.entities.Season;
import seal.backend.requests.CreateSeasonRequest;

public interface SeasonsService {
  public List<Season> getAll();

  public Season create(CreateSeasonRequest request);
}
