package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
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

  void acceptDeviation(UUID submissionId, UUID notifId);

  void rejectDeviation(UUID submissionId, UUID notifId, String reason);
}
