package seal.backend.services.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import seal.backend.entities.AuditLog;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.services.AuditLogService;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
  private final AuditLogRepository<AuditLog> auditLogRepository;

  @Override
  public List<Object> getAllAuditLogs() {
    List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "actionTime"));
    return logs.stream().map(AuditLog::toDto).toList();
  }
}
