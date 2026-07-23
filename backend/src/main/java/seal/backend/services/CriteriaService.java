package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaTemplateDto;

public interface CriteriaService {
  CriteriaTemplateDto[] getAllTemplates();

  CriteriaTemplateDto createTemplate(CreateCriteriaTemplateRequestDto request);

  CriteriaTemplateDto updateTemplate(UUID templateId, CreateCriteriaTemplateRequestDto request);

  void deleteTemplate(UUID templateId);
}
