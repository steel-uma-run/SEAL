package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.services.SeasonsService;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final SeasonRepository seasonRepository;

  @Override
  public List<Season> getAll() {
    return seasonRepository.findAll();
  }
}
