package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Season;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.requests.CreateEventRequest;
import seal.backend.services.HackathonEventService;

@Service
@RequiredArgsConstructor
public class HackathonEventServiceImpl implements HackathonEventService {
  private final HackathonEventRepository hackathonEventRepository;
  private final SeasonRepository seasonRepository;

  @Override
  public List<HackathonEvent> getAllEvents() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean isCoordinator =
        auth != null
            && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("COORDINATOR"));

    if (isCoordinator) {
      return hackathonEventRepository.findAll();
    }

    return hackathonEventRepository.findByStatus(EventStatus.FINALIZED);
  }

  @Override
  public HackathonEvent createEvent(CreateEventRequest request) {
    if (request.endTime().isEqual(request.startTime())
        || request.endTime().isBefore(request.startTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "End time must be after start time");
    }

    Season season =
        seasonRepository
            .findById(request.seasonId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));

    HackathonEvent hackathonEvent =
        new HackathonEvent(
            request.name(),
            request.description(),
            request.startTime(),
            request.endTime(),
            EventStatus.DRAFT,
            season);

    return hackathonEventRepository.save(hackathonEvent);
  }
}
