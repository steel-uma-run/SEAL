package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.requests.CreateSeasonRequest;
import seal.backend.services.SeasonService;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {
  private final SeasonRepository seasonRepository;

  @Override
  public Season createSeason(CreateSeasonRequest request) {
    //    if (request.endTime().isEqual(request.startTime())
    //        || request.endTime().isBefore(request.startTime())) {
    //      throw new ResponseStatusException(
    //          HttpStatus.BAD_REQUEST, "Season end time must be after start time.");
    //    }
    //
    //    OffsetDateTime now = OffsetDateTime.now();
    //    SeasonStatus status;
    //
    //    if (now.isBefore(request.startTime())) {
    //      status = SeasonStatus.UPCOMING;
    //    } else if (now.isAfter(request.endTime())) {
    //      status = SeasonStatus.COMPLETED;
    //    } else {
    //      status = SeasonStatus.ONGOING;
    //    }
    //
    Season season = new Season();

    return seasonRepository.save(season);
  }

  @Override
  public List<Season> getAllSeasons() {
    return seasonRepository.findAll();
  }
}
