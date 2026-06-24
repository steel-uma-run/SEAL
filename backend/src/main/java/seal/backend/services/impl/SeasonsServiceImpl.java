package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.enums.Semester;
import seal.backend.repositories.SeasonRepository;
import seal.backend.services.SeasonsService;
import seal.openapi.model.CreateSeasonRequestDto;
import seal.openapi.model.SeasonDto;
import seal.openapi.model.SeasonSemesterDto;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final SeasonRepository seasonRepository;

  @Override
  public List<SeasonDto> getAllSeasons() {
    List<Season> seasonEntities = seasonRepository.findAll();
    List<SeasonDto> resultList = new ArrayList<>();

    for (Season season : seasonEntities) {
      SeasonDto dto =
          new SeasonDto(
              season.getId(),
              SeasonSemesterDto.fromValue(season.getSemester().name()),
              season.getYear());
      resultList.add(dto);
    }

    return resultList;
  }

  @Override
  public SeasonDto getSeason(UUID seasonId) {
    Season season =
        seasonRepository
            .findById(seasonId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season does not exist"));
    return new SeasonDto(
        season.getId(), SeasonSemesterDto.fromValue(season.getSemester().name()), season.getYear());
  }

  @Override
  @Transactional
  public SeasonDto createSeason(CreateSeasonRequestDto request) {
    boolean isDuplicate =
        seasonRepository.existsBySemesterAndYear(
            Semester.valueOf(request.semester().name()), request.year());

    if (isDuplicate) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "The " + request.semester().name() + " " + request.year() + " season already exists.");
    }

    Season newSeason = new Season(Semester.valueOf(request.semester().name()), request.year());
    Season savedSeason = seasonRepository.save(newSeason);

    return new SeasonDto(
        savedSeason.getId(),
        SeasonSemesterDto.fromValue(savedSeason.getSemester().name()),
        savedSeason.getYear());
  }
}
