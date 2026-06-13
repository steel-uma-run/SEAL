package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import seal.backend.entities.HackathonEvent;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.requests.CreateSeasonRequest;
import seal.backend.services.SeasonsService;

@Service
@RequiredArgsConstructor
public class SeasonsServiceImpl implements SeasonsService {
  private final HackathonEventRepository hackathonEventRepository;

  @Override
  public List<HackathonEvent> getAll() {
    return hackathonEventRepository.findAll();
  }

  @Override
  public HackathonEvent create(CreateSeasonRequest request) {
    if (request.endTime().isEqual(request.startTime())
        || request.endTime().isBefore(request.startTime())) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    HackathonEvent hackathonEvent =
        new HackathonEvent(
            request.name(), request.description(), request.startTime(), request.endTime());

    return hackathonEventRepository.save(hackathonEvent);
  }
}
