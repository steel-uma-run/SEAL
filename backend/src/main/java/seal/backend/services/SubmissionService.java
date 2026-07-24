package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
import seal.openapi.model.RequestRegradePayloadDto;
import seal.openapi.model.ScoreDeviationNotifDto;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.SubmitWorkRequestDto;

public interface SubmissionService {
  void submitWork(UUID eventId, SubmitWorkRequestDto request);

  SubmissionDto getSubmissionById(UUID submissionId);

  List<SubmissionDto> getSubmissionsByEventId(UUID eventId);

  List<SubmissionDto> getAllSubmissions(UUID teamId);

  void gradeSubmission(UUID submissionId, GradeSubmissionRequestArrayItemDto[] scores);

  List<ScoreDeviationNotifDto> getScoreDeviations(UUID submissionId);

  void requestRegrade(UUID submissionId, RequestRegradePayloadDto payload);

  void approveRegrade(UUID submissionId);
}
