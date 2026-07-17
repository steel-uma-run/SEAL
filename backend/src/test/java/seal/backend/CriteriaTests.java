package seal.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.services.CriteriaService;
import seal.openapi.model.CreateCriteriaRequestDto;
import seal.openapi.model.CreateCriteriaTemplateRequestDto;

@SpringBootTest
@AutoConfigureMockMvc
class CriteriaTests {
  @Autowired private CriteriaService criteriaService;
  @Autowired private CreateUtils createUtils;

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
}
