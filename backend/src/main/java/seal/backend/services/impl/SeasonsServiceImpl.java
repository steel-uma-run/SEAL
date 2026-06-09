package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.requests.CreateSeasonRequest;
import seal.backend.services.SeasonsService;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final SeasonRepository seasonRepository;

  @Override
  public List<Season> getAll() {
    return seasonRepository.findAll();
  }

  @Override
  public Season create(CreateSeasonRequest request) {
    if (request.endTime().isEqual(request.startTime())
        || request.endTime().isBefore(request.startTime())) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    Season season =
        new Season(request.name(), request.description(), request.startTime(), request.endTime());
    return seasonRepository.save(season);
  }
}
