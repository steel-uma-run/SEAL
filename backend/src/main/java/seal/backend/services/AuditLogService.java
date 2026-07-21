package seal.backend.services;

import java.util.List;
import seal.openapi.model.AuditLogDto;

public interface AuditLogService {
  List<AuditLogDto> getAllAuditLogs();
}
