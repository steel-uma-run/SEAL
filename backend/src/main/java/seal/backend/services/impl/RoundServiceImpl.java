package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.Duration;
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

    Round round =
        new Round(
            request.name(),
            request.description(),
            Duration.ofMillis(request.activeDuration()),
            Duration.ofMillis(request.gradingDuration()),
            event);

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

    if (request.name() != null) {
      round.setName(request.name());
    }
    if (request.description() != null) {
      round.setDescription(request.description());
    }
    if (request.activeDuration() != null) {
      round.setActiveDuration(Duration.ofMillis(request.activeDuration()));
    }
    if (request.gradingDuration() != null) {
      round.setGradingDuration(Duration.ofMillis(request.gradingDuration()));
    }

    return roundRepository.save(round).toDto();
  }

  @Override
  @Transactional
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

    // clear all criteria assigned to this round before deletion
    for (Criteria criteria : round.getCriteria()) {
      criteriaRepo.delete(criteria);
    }
    round.getCriteria().clear();

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
      Criteria savedCriteria = criteriaRepo.save(newCriteria);
      criteriaSet.add(savedCriteria);
    }

    round.setCriteria(criteriaSet);
    roundRepository.save(round);
  }
}
