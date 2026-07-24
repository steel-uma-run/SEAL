package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Criteria;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Round;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.services.RoundService;
import seal.openapi.model.AssignCriteriaRequestArrayItemDto;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;
import seal.openapi.model.UpdateRoundRequestDto;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {
  private final HackathonEventRepository eventRepository;
  private final RoundRepository roundRepository;
  private final CriteriaRepository criteriaRepo;

  @Override
  public RoundDto createRound(UUID eventId, CreateRoundRequestDto request) {
    HackathonEvent event =
        eventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot create round. Event is not in DRAFT status.");
    }

    if (request.startTime().isAfter(request.endTime())
        || request.startTime().isEqual(request.endTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Start time must be before end time.");
    }

    if (!request.startTime().isAfter(event.getEndTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Round timeframe must start after the registration period ends.");
    }

    boolean isOverlapping =
        roundRepository.existsByEventIdAndStartTimeLessThanAndEndTimeGreaterThan(
            eventId, request.startTime(), request.endTime());

    if (isOverlapping) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The round time overlaps with an existing round in this event.");
    }

    Round round =
        new Round(
            request.name(), request.startTime(), request.endTime(), request.description(), event);

    Round savedRound = roundRepository.save(round);

    return savedRound.toDto();
  }

  @Override
  @Transactional
  public RoundDto updateRound(UUID roundId, UpdateRoundRequestDto request) {
    Round round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found."));

    if (round.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update round. Event is not in DRAFT status.");
    }

    if (request.startTime().isAfter(request.endTime())
        || request.startTime().isEqual(request.endTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Start time must be before end time.");
    }

    if (!request.startTime().isAfter(round.getEvent().getEndTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Round timeframe must start after the registration period ends.");
    }

    List<Round> allRounds = roundRepository.findByEventId(round.getEvent().getId());
    for (Round existingRound : allRounds) {
      if (!existingRound.getId().equals(round.getId())) {
        if (request.startTime().isBefore(existingRound.getEndTime())
            && request.endTime().isAfter(existingRound.getStartTime())) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "The round time overlaps with an existing round in this event.");
        }
      }
    }

    round.setName(request.name());
    round.setDescription(request.description());
    round.setStartTime(request.startTime());
    round.setEndTime(request.endTime());

    return roundRepository.save(round).toDto();
  }

  @Override
  public void deleteRound(UUID roundId) {
    Round round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

    if (round.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete round. Event is not in DRAFT status.");
    }

    roundRepository.delete(round);
  }

  @Override
  @Transactional
  public List<RoundDto> getRounds(UUID eventId) {
    return roundRepository.findByEventId(eventId).stream().map(round -> round.toDto()).toList();
  }

  @Override
  @Transactional
  public void assignCriteria(UUID roundId, AssignCriteriaRequestArrayItemDto[] criteriaDtos) {
    Round round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

    if (round.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot assign criteria. Event is not in DRAFT status.");
    }

    // clear all existing criteria of this round (if any)
    for (Criteria criteria : round.getCriteria()) {
      criteriaRepo.delete(criteria);
    }
    round.getCriteria().clear();

    // create new criteria from the dtos
    List<Criteria> criteriaSet = new ArrayList<>();

    for (AssignCriteriaRequestArrayItemDto dto : criteriaDtos) {
      Criteria newCriteria = new Criteria(dto.name(), dto.description(), dto.weight(), round);
      criteriaRepo.save(newCriteria);
    }

    round.setCriteria(criteriaSet);
    roundRepository.save(round);
  }
}
