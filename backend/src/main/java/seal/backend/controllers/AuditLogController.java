package seal.backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.AuditLogService;
import seal.openapi.api.AuditApi;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AuditLogController implements AuditApi {
  private final AuditLogService auditLogService;

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Object[]> getAllAuditLogs() {
    List<Object> logs = auditLogService.getAllAuditLogs();
    return ResponseEntity.ok(logs.toArray(new Object[0]));
  }
}
