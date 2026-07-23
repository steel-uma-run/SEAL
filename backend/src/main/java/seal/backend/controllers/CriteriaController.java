package seal.backend.controllers;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.CriteriaService;
import seal.openapi.api.CriteriaApi;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaTemplateDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class CriteriaController implements CriteriaApi {
  private final CriteriaService criteriaService;

  @Override
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CriteriaTemplateDto[]> getAllCriteriaTemplates() {
    return ResponseEntity.ok(criteriaService.getAllTemplates());
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<CriteriaTemplateDto> createCriteriaTemplate(
      @RequestBody CreateCriteriaTemplateRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(criteriaService.createTemplate(request));
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<CriteriaTemplateDto> updateCriteriaTemplate(
      UUID templateId, CreateCriteriaTemplateRequestDto request) {
    return ResponseEntity.ok(criteriaService.updateTemplate(templateId, request));
  }

  @Override
  @PreAuthorize("hasRole('COORDINATOR')")
  public ResponseEntity<Void> deleteCriteriaTemplate(UUID templateId) {
    criteriaService.deleteTemplate(templateId);
    return ResponseEntity.noContent().build();
  }
}
