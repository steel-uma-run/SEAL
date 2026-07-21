package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
import seal.openapi.api.SubmissionApi;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
import seal.openapi.model.ScoreDeviationNotifDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class SubmissionController implements SubmissionApi {
  private final SubmissionService submissionService;

  @Override
  @PreAuthorize("hasRole('LECTURER')")
  public ResponseEntity<Void> gradeSubmission(
      @PathVariable(name = "submissionId") @NotNull UUID submissionId,
      @RequestBody @Valid @NotNull GradeSubmissionRequestArrayItemDto[] body) {
    submissionService.gradeSubmission(submissionId, body);
    return ResponseEntity.noContent().build();
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<ScoreDeviationNotifDto[]> getScoreDeviations(
      @PathVariable(name = "submissionId") @NotNull UUID submissionId) {

    List<ScoreDeviationNotifDto> deviations = submissionService.getScoreDeviations(submissionId);
    return ResponseEntity.ok(deviations.toArray(ScoreDeviationNotifDto[]::new));
  }
}
