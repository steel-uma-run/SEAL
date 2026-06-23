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
import seal.backend.services.SubmissionService;
import seal.openapi.api.SubmissionsApi;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.SubmitWorkRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class SubmissionController implements SubmissionsApi {
  private final SubmissionService submissionService;

  @Override
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<Void> submitWork(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId,
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull SubmitWorkRequestDto body) {
    submissionService.submitWork(eventId, body);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER')")
  public ResponseEntity<SubmissionDto[]> getAllSubmissions(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId,
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @PathVariable(name = "teamId") @NotNull UUID teamId) {
    return ResponseEntity.ok(
        submissionService.getAllSubmissions(teamId).toArray(SubmissionDto[]::new));
  }
}
