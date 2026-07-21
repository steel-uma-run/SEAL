package seal.backend.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.AuditLogService;
import seal.openapi.model.AuditLogDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class AuditLogController {
  private final AuditLogService auditLogService;

  @GetMapping("/auditlogs")
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
    return ResponseEntity.ok(auditLogService.getAllAuditLogs());
  }
}
