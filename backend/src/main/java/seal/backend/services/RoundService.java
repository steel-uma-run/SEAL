package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.AssignCriteriaRequestArrayItemDto;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;
import seal.openapi.model.UpdateRoundRequestDto;

public interface RoundService {
  RoundDto createRound(UUID eventId, CreateRoundRequestDto request);

  RoundDto updateRound(UUID roundId, UpdateRoundRequestDto request);

  void deleteRound(UUID roundId);

  List<RoundDto> getRounds(UUID eventId);

  void assignCriteria(UUID roundId, AssignCriteriaRequestArrayItemDto[] criteriaDtos);
}
