package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.SubmitWorkRequestDto;

public interface SubmissionService {
  void submitWork(UUID eventId, SubmitWorkRequestDto request);

  List<SubmissionDto> getAllSubmissions(UUID teamId);

  void gradeSubmission(UUID submissionId, GradeSubmissionRequestArrayItemDto[] scores);
}
