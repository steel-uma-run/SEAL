package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.RoundService;
import seal.openapi.api.RoundsApi;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.RoundDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class RoundController implements RoundsApi {
  private final RoundService roundService;

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<RoundDto> createRound(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId,
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull CreateRoundRequestDto request) {

    RoundDto responseDto = roundService.createRound(seasonId, eventId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> deleteRound(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId,
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @PathVariable(name = "roundId") @NotNull UUID roundId) {

    roundService.deleteRound(seasonId, eventId, roundId);
    return ResponseEntity.ok().build();
  }
}
