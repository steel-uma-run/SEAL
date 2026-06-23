package seal.backend.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import seal.backend.services.HackathonEventService;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.HackathonEventStatusDto;

@Service
@RequiredArgsConstructor
public class HackathonEventServiceImpl implements HackathonEventService {
  private final HackathonEventRepository hackathonEventRepository;
  private final SeasonRepository seasonRepository;

  @Override
  public void finalizeEvent(UUID seasonId, UUID eventId) {
    seasonRepository
        .findById(seasonId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() == EventStatus.FINALIZED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Already finalized");
    }

    event.setStatus(EventStatus.FINALIZED);
  }

  @Override
  public HackathonEventDto getEvent(UUID seasonId, UUID eventId) {
    seasonRepository
        .findById(seasonId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
    hackathonEventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    Optional<HackathonEvent> result = hackathonEventRepository.findById(eventId);

    HackathonEventDto dto =
        new HackathonEventDto(
            result.get().getId(),
            result.get().getName(),
            result.get().getDescription(),
            HackathonEventStatusDto.fromValue(result.get().getStatus().name()),
            result.get().getStartTime(),
            result.get().getEndTime(),
            result.get().getSeason().getId());

    return dto;
  }

  @Override
  public List<HackathonEventDto> getAllEvents() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean isCoordinator =
        auth != null
            && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("COORDINATOR"));

    List<HackathonEvent> found = new ArrayList<>();
    List<HackathonEventDto> resultList = new ArrayList<>();

    if (isCoordinator) {
      found = hackathonEventRepository.findAll();
    } else {
      found = hackathonEventRepository.findByStatus(EventStatus.FINALIZED);
    }

    for (HackathonEvent event : found) {
      HackathonEventDto dto =
          new HackathonEventDto(
              event.getId(),
              event.getName(),
              event.getDescription(),
              HackathonEventStatusDto.fromValue(event.getStatus().name()),
              event.getStartTime(),
              event.getEndTime(),
              event.getSeason().getId());
      resultList.add(dto);
    }

    return resultList;
  }

  @Override
  public HackathonEventDto createEvent(CreateEventRequestDto request) {
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

    return new HackathonEventDto(
        hackathonEvent.getId(),
        hackathonEvent.getName(),
        hackathonEvent.getDescription(),
        HackathonEventStatusDto.fromValue(hackathonEvent.getStatus().name()),
        hackathonEvent.getStartTime(),
        hackathonEvent.getEndTime(),
        hackathonEvent.getSeason().getId());
  }
}
