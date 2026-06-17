package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.services.SeasonsService;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final SeasonRepository seasonRepository;

  @Override
  public List<Season> getAllSeasons() {
    return seasonRepository.findAll();
  }

  @Override
  public Season getSeason(UUID seasonId) {
    return seasonRepository
        .findById(seasonId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season does not exist"));
  }

  @Override
  @Transactional
  public Season createSeason(Season newSeason) {
    boolean isDuplicate =
        seasonRepository.existsBySemesterAndYear(newSeason.getSemester(), newSeason.getYear());
    if (isDuplicate) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "The " + newSeason.getSemester() + newSeason.getYear() + " season already exists.");
    }

    return seasonRepository.save(newSeason);
  }
}
