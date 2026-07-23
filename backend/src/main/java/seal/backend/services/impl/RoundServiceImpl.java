package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.HashSet;
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
import seal.backend.entities.CriteriaTemplate;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;
import seal.openapi.model.UpdateRoundCriteriaRequestDto;
import seal.openapi.model.CriteriaDto;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {
  private final HackathonEventRepository eventRepository;
  private final RoundRepository roundRepository;
  private final CriteriaRepository criteriaRepo;
  private final CriteriaTemplateRepository criteriaTemplateRepository;

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
  public void assignCriteria(UUID roundId, UUID[] criteriaIds) {
    Round round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

    if (round.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot assign criteria. Event is not in DRAFT status.");
    }

    List<Criteria> criterias = criteriaRepo.findAllById(Arrays.asList(criteriaIds));
    if (criterias.size() < criteriaIds.length) {
      // some IDs were nonexistent
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some criteria were invalid");
    }

    round.setCriteria(new HashSet<>(criterias));
    roundRepository.save(round);
  }

  @Override
  @Transactional
  public void updateRoundCriteria(UUID roundId, UpdateRoundCriteriaRequestDto request) {
    Round round =
        roundRepository
            .findById(roundId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

    if (round.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update criteria. Event is not in DRAFT status.");
    }

    if (request.criteria() == null || request.criteria().length == 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Criteria list cannot be empty.");
    }

    // Validate that the sum of the weights of all criteria equals exactly 100
    int totalWeight = 0;
    for (CriteriaDto dto : request.criteria()) {
      if (dto.weight() == null || dto.weight() < 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria weight must be a non-negative integer.");
      }
      totalWeight += dto.weight();
    }
    if (totalWeight != 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The total weight of all criteria must sum to exactly 100.");
    }

    // Create a new CriteriaTemplate for this customized set.
    CriteriaTemplate customTemplate = new CriteriaTemplate("Custom for Round: " + round.getName());
    customTemplate = criteriaTemplateRepository.save(customTemplate);

    HashSet<Criteria> newCriteriaSet = new HashSet<>();
    for (CriteriaDto dto : request.criteria()) {
      Criteria criteria = new Criteria(dto.name(), dto.weight(), customTemplate);
      newCriteriaSet.add(criteriaRepo.save(criteria));
    }

    // Set the new criteria set on the round
    round.setCriteria(newCriteriaSet);
    roundRepository.save(round);
  }
}
