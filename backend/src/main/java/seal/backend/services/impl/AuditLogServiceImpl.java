package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seal.backend.entities.AuditLog;
import seal.backend.entities.audit.AccountApprovedLog;
import seal.backend.entities.audit.AccountBannedLog;
import seal.backend.entities.audit.GradingLog;
import seal.backend.entities.audit.SubmissionLog;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.services.AuditLogService;
import seal.openapi.model.AuditLogDto;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogRepository<AuditLog> auditLogRepository;

  @Override
  public List<AuditLogDto> getAllAuditLogs() {
    List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "actionTime"));

    return logs.stream()
        .map(
            log -> {
              String logType = "UNKNOWN";
              String actorEmail = "System";
              String details = "";

              if (log instanceof AccountApprovedLog approvedLog) {
                logType = "ACCOUNT_APPROVED";
                actorEmail = approvedLog.getActor().getEmail();
                details = "Approved student: " + approvedLog.getApprovedStudent().getEmail();
              } else if (log instanceof AccountBannedLog bannedLog) {
                logType = "ACCOUNT_BANNED";
                actorEmail = bannedLog.getActor().getEmail();
                details =
                    "Banned student: "
                        + bannedLog.getBanTarget().getEmail()
                        + " - Reason: "
                        + bannedLog.getBanReason();
              } else if (log instanceof SubmissionLog subLog) {
                logType = "SUBMISSION";
                actorEmail = subLog.getActor().getEmail();
                details =
                    "Submitted work for event: "
                        + subLog.getSubmission().getRound().getEvent().getName();
              } else if (log instanceof GradingLog gradLog) {
                logType = gradLog.getAction();
                actorEmail = gradLog.getActor().getEmail();
                details =
                    gradLog.getDetails() != null
                        ? gradLog.getDetails()
                        : "Grading action performed";
              }

              return new AuditLogDto(
                  log.getId(), log.getActionTime(), logType, actorEmail, details);
            })
        .toList();
  }
}
