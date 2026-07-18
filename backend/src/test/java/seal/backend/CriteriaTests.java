package seal.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.entities.HackathonEvent;
import seal.backend.enums.EventStatus;
import seal.backend.services.CriteriaService;
import seal.backend.services.RoundService;
import seal.openapi.model.CreateCriteriaRequestDto;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CriteriaTests {
  @Autowired private CriteriaService criteriaService;
  @Autowired private RoundService roundService;
  @Autowired private CreateUtils createUtils;
  @Autowired private EntityManager em;

  @Test
  @Transactional
  void smokeTest() throws Exception {
    List<CreateCriteriaRequestDto> criterias = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      criterias.add(new CreateCriteriaRequestDto(createUtils.randomString(10), 20));
    }

    criteriaService.createTemplate(
        new CreateCriteriaTemplateRequestDto(
            createUtils.randomString(50), criterias.toArray(CreateCriteriaRequestDto[]::new)));

    assertEquals(1, criteriaService.getAllTemplates().length);
  }

  @Test
  @Transactional
  void weightsMustSumTo100() throws Exception {
    List<CreateCriteriaRequestDto> criterias = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      criterias.add(new CreateCriteriaRequestDto(createUtils.randomString(10), 50));
    }

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                criteriaService.createTemplate(
                    new CreateCriteriaTemplateRequestDto(
                        createUtils.randomString(50),
                        criterias.toArray(CreateCriteriaRequestDto[]::new))));

    assertTrue(ex.getMessage().contains("sum to 100"));
  }

  @Test
  @Transactional
  void assignToRound() throws Exception {
    CriteriaTemplate template = createUtils.createCriteriaTemplate();
    HackathonEvent event = createUtils.createOngoingEvent();

    event.setStatus(EventStatus.DRAFT);

    roundService.assignCriteria(
        event.getActiveRound().get().getId(),
        template.getCriteria().stream().map(crit -> crit.getId()).toArray(UUID[]::new));
  }
}
