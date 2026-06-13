package seal.backend.services;

import java.util.List;
import seal.backend.entities.HackathonEvent;
import seal.backend.requests.CreateSeasonRequest;

public interface SeasonsService {
  public List<HackathonEvent> getAll();

  public HackathonEvent create(CreateSeasonRequest request);
}
