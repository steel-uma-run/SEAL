package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;

public interface RoundService {
  RoundDto createRound(UUID eventId, CreateRoundRequestDto request);

  void deleteRound(UUID roundId);
}
