package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Criteria;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.services.CriteriaService;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;
import seal.openapi.model.CriteriaDto;
import seal.openapi.model.CriteriaTemplateDto;

@Service
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {
  private final CriteriaTemplateRepository templateRepo;

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
    CriteriaTemplate newTemplate = new CriteriaTemplate(request.description());

    // Constraint: all criteria in a template must sum to 100
    int totalWeight = 0;

    for (CriteriaDto dto : request.criteria()) {
      totalWeight += dto.weight();
      if (totalWeight > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
      }

      newTemplate.getCriteria().add(new Criteria(dto.name(), dto.weight()));
    }

    if (totalWeight != 100) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "All criteria's weights must sum to 100");
    }

    return templateRepo.save(newTemplate).toDto();
  }
}
