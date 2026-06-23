package seal.backend.services;

import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaTemplateDto;

public interface CriteriaService {
  CriteriaTemplateDto[] getAllTemplates();

  CriteriaTemplateDto createTemplate(CreateCriteriaTemplateRequestDto request);
}
