package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.entities.TemplatedCriteria;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.repositories.TemplatedCriteriaRepository;
import seal.backend.services.CriteriaService;
import seal.openapi.model.CreateCriteriaTemplateRequestCriteriaArrayItemDto;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaTemplateDto;

@Service
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {
  private final CriteriaTemplateRepository templateRepo;
  private final TemplatedCriteriaRepository templatedCriteriaRepo;

  @Transactional
  @Override
  public CriteriaTemplateDto[] getAllTemplates() {
    return templateRepo.findAll().stream()
        .map(CriteriaTemplate::toDto)
        .toArray(CriteriaTemplateDto[]::new);
  }

  @Transactional
  @Override
  public CriteriaTemplateDto createTemplate(CreateCriteriaTemplateRequestDto request) {
    int totalWeight = 0;
    CriteriaTemplate newTemplate =
        templateRepo.save(new CriteriaTemplate(request.name(), request.description()));

    for (CreateCriteriaTemplateRequestCriteriaArrayItemDto dto : request.criteria()) {
      totalWeight += dto.weight();
      if (totalWeight > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
      }

      TemplatedCriteria newCrit =
          new TemplatedCriteria(dto.name(), dto.description(), dto.weight(), newTemplate);

      templatedCriteriaRepo.save(newCrit);
      newTemplate.getCriteria().add(newCrit);
    }

    if (totalWeight != 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
    }

    return templateRepo.save(newTemplate).toDto();
  }

  @Transactional
  @Override
  public CriteriaTemplateDto updateTemplate(
      UUID templateId, CreateCriteriaTemplateRequestDto request) {
    CriteriaTemplate template =
        templateRepo
            .findById(templateId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Criteria template not found"));

    template.setDescription(request.description());

    // clear all criteria belonging to this template
    for (TemplatedCriteria criteria : template.getCriteria()) {
      templatedCriteriaRepo.delete(criteria);
    }
    template.getCriteria().clear();

    int totalWeight = 0;
    for (CreateCriteriaTemplateRequestCriteriaArrayItemDto dto : request.criteria()) {
      totalWeight += dto.weight();
      if (totalWeight > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
      }

      TemplatedCriteria newCrit =
          new TemplatedCriteria(dto.name(), dto.description(), dto.weight(), template);

      templatedCriteriaRepo.save(newCrit);
      template.getCriteria().add(newCrit);
    }

    if (totalWeight != 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
    }

    return templateRepo.save(template).toDto();
  }

  @Transactional
  @Override
  public void deleteTemplate(UUID templateId) {
    CriteriaTemplate template =
        templateRepo
            .findById(templateId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Criteria template not found"));

    for (TemplatedCriteria criteria : template.getCriteria()) {
      templatedCriteriaRepo.delete(criteria);
    }

    templateRepo.delete(template);
  }
}
