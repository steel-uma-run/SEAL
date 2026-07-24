package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.RoundService;
import seal.openapi.api.RoundsApi;
import seal.openapi.model.AssignCriteriaRequestArrayItemDto;
import seal.openapi.model.RoundDto;
import seal.openapi.model.UpdateRoundRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class RoundController implements RoundsApi {
  private final RoundService roundService;

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<RoundDto> updateRound(
      UUID roundId, @RequestBody @Valid @NotNull UpdateRoundRequestDto body) {
    return ResponseEntity.ok(roundService.updateRound(roundId, body));
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> deleteRound(@PathVariable(name = "roundId") @NotNull UUID roundId) {
    roundService.deleteRound(roundId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> assignCriteria(
      @PathVariable(name = "roundId") @NotNull UUID roundId,
      @RequestBody @Valid @NotNull AssignCriteriaRequestArrayItemDto[] body) {
    roundService.assignCriteria(roundId, body);
    return ResponseEntity.noContent().build();
  }
}
