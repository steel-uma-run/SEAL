package seal.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seal.backend.config.GlobalConfig;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamRepository;
import seal.openapi.model.CreateTeamRequestPayloadDto;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.SubmitWorkRequestDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class SubmissionControllerTests {
  @Autowired private MockMvc mvc;
  @Autowired private TeamRepository teamRepo;
  @Autowired private StudentRepository studentRepo;
  @Autowired private CreateUtils createUtils;
  @Autowired private PasswordEncoder passwordEncoder;

  private final ObjectMapper objMapper = new ObjectMapper();

  private HackathonEvent testEvent;
  private HackathonEvent ongoingEvent;

  private String loginAndGetToken(String email, String password) throws Exception {
    ResultActions result =
        mvc.perform(
                MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objMapper.writeValueAsString(new LoginRequestPayloadDto(email, password))))
            .andExpect(MockMvcResultMatchers.status().isOk());

    String body = result.andReturn().getResponse().getContentAsString();
    String token = (new JacksonJsonParser()).parseMap(body).get("token").toString();

    return token;
  }

  @BeforeAll
  void setup() throws Exception {
    testEvent = createUtils.createFinalizedEvent();
    ongoingEvent = createUtils.createOngoingEvent();

    Student student = createUtils.createStudent();
    student.setEmail("student@example.com");
    student.setPasswordHash(passwordEncoder.encode("hunter2"));
    student.setStudentStatus(StudentStatus.ACTIVE);
    studentRepo.save(student);
  }

  @Test
  @Order(1)
  void createTeam() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    // Create 2 teams, one in each and one in ongoingEvent
    mvc.perform(
            MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new CreateTeamRequestPayloadDto("Team", "", testEvent.getId(), null, null)))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(MockMvcResultMatchers.status().isCreated());

    mvc.perform(
            MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new CreateTeamRequestPayloadDto(
                            "Team", "", ongoingEvent.getId(), null, null)))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(MockMvcResultMatchers.status().isCreated());
  }

  @Test
  @Order(2)
  void approveTeam() throws Exception {
    for (Team team : teamRepo.findAll()) {
      team.setTeamStatus(TeamStatus.APPROVED);
      teamRepo.save(team);
    }
  }

  @Test
  @Order(3)
  void submitWorkBadLinks() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE + "/events/" + testEvent.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(new SubmitWorkRequestDto("Title", "", "", "", "")))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(
            MockMvcResultMatchers.status().isBadRequest(),
            MockMvcResultMatchers.jsonPath(
                "$.detail", Matchers.is("GitHub link must be a valid HTTP(S) link to GitHub")));
  }

  @Test
  @Order(3)
  void submitWorkEventNotOngoing() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE + "/events/" + testEvent.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new SubmitWorkRequestDto(
                            "Title",
                            "desc",
                            "https://github.com",
                            "youtube.com/watch?v=dQw4w9WgXcQ",
                            "")))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(
            MockMvcResultMatchers.status().isBadRequest(),
            MockMvcResultMatchers.jsonPath("$.detail", Matchers.is("Event is not ongoing.")));
  }

  @Test
  @Order(3)
  void submitWorkNotEligible() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE + "/events/" + ongoingEvent.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new SubmitWorkRequestDto(
                            "Title",
                            "desc",
                            "https://github.com",
                            "youtube.com/watch?v=dQw4w9WgXcQ",
                            "")))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(
            MockMvcResultMatchers.status().isBadRequest(),
            MockMvcResultMatchers.jsonPath(
                "$.detail", Matchers.is("Team is not eligible to participate.")));
  }

  @Test
  @Order(4)
  void addMembersToTeam() {
    for (Team team : teamRepo.findAll()) {
      for (int i = 0; i < 3; i++) {
        Student student = createUtils.createStudent();
        team.getMembers().add(student);
      }

      teamRepo.save(team);
    }
  }

  @Test
  @Order(5)
  void submitWork() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE + "/events/" + ongoingEvent.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new SubmitWorkRequestDto(
                            "Title",
                            "desc",
                            "https://github.com",
                            "youtube.com/watch?v=dQw4w9WgXcQ",
                            "")))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(MockMvcResultMatchers.status().isOk());
  }
}
