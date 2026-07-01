package seal.backend.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Round;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.services.RoundService;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {
  private final HackathonEventRepository eventRepository;
  private final RoundRepository roundRepository;

  @Override
  public RoundDto createRound(UUID seasonId, UUID eventId, CreateRoundRequestDto request) {
    HackathonEvent event =
        eventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (!event.getSeason().getId().equals(seasonId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Event does not belong to the specified season.");
    }

    if (event.getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot create round. Event is not in DRAFT status.");
    }

    if (request.startTime().isAfter(request.endTime())
        || request.startTime().isEqual(request.endTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Start time must be before end time.");
    }

    boolean isOverlapping =
        roundRepository.existsByEventIdAndStartTimeLessThanAndEndTimeGreaterThan(
            eventId, request.endTime(), request.startTime());

    if (isOverlapping) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The round time overlaps with an existing round in this event.");
    }

    Round round =
        new Round(
            request.name(), request.startTime(), request.endTime(), request.description(), event);

    Round savedRound = roundRepository.save(round);

    return new RoundDto(
        savedRound.getId(),
        savedRound.getName(),
        savedRound.getDescription(),
        savedRound.getStartTime(),
        savedRound.getEndTime(),
        event.getId());
  }
}
