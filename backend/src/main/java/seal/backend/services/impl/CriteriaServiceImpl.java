package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Criteria;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.services.CriteriaService;
import seal.openapi.model.CreateCriteriaRequestDto;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaTemplateDto;

@Service
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {
  private final CriteriaTemplateRepository templateRepo;
  private final CriteriaRepository criteriaRepo;
  private final RoundRepository roundRepo;

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
    if (request.description() == null || request.description().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The template's description must not be empty.");
    }
    CriteriaTemplate newTemplate = templateRepo.save(new CriteriaTemplate(request.description()));

    int totalWeight = 0;

    for (CreateCriteriaRequestDto dto : request.criteria()) {
      if (dto.name() == null || dto.name().isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's name must not be empty.");
      }

      if (dto.weight() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's weight must not be empty.");
      }

      if (dto.weight() <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's weight must be greater than 0.");
      }

      totalWeight += dto.weight();
      if (totalWeight > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
      }

      Criteria newCrit = new Criteria(dto.name(), dto.weight(), newTemplate);
      criteriaRepo.save(newCrit);
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

    if (roundRepo.existsByCriteria_CriteriaTemplate_IdAndEvent_StatusNot(
        templateId, EventStatus.DRAFT)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot update a template that is assigned to an event which is already finalized or active.");
    }

    if (request.description() == null || request.description().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The template's description must not be empty.");
    }
    template.setDescription(request.description());

    criteriaRepo.deleteByCriteriaTemplate(template);

    template.getCriteria().clear();

    int totalWeight = 0;
    for (CreateCriteriaRequestDto dto : request.criteria()) {
      if (dto.name() == null || dto.name().isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's name must not be empty.");
      }

      if (dto.weight() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's weight must not be empty.");
      }

      if (dto.weight() <= 0) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Criteria's weight must be greater than 0.");
      }

      totalWeight += dto.weight();
      if (totalWeight > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
      }

      Criteria newCrit = new Criteria(dto.name(), dto.weight(), template);
      criteriaRepo.save(newCrit);
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

    if (roundRepo.existsByCriteria_CriteriaTemplate_IdAndEvent_StatusNot(
        templateId, EventStatus.DRAFT)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot delete a template that is assigned to an event which is already finalized or active.");
    }

    criteriaRepo.deleteByCriteriaTemplate(template);
    templateRepo.delete(template);
  }
}
