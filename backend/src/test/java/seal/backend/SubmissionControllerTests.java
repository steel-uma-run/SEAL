package seal.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seal.backend.config.GlobalConfig;
import seal.backend.entities.Coordinator;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Round;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.User;
import seal.backend.enums.EventStatus;
import seal.backend.enums.Role;
import seal.backend.enums.SeasonStatus;
import seal.backend.enums.Semester;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.CoordinatorRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.openapi.model.CreateTeamRequestDto;
import seal.openapi.model.LoginRequestPayloadDto;
import seal.openapi.model.SubmitWorkRequestDto;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class SubmissionControllerTests {
  @Autowired private MockMvc mvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private CoordinatorRepository coordRepo;
  @Autowired private UserRepository userRepo;
  @Autowired private StudentRepository studentRepo;
  @Autowired private SeasonRepository seasonRepo;
  @Autowired private HackathonEventRepository eventRepo;
  @Autowired private TeamRepository teamRepo;
  @Autowired private RoundRepository roundRepo;

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
  void setupAccounts() throws Exception {
    {
      User user =
          new User(
              "Coordinator",
              Role.COORDINATOR,
              "coordinator@example.com",
              passwordEncoder.encode("admin"));
      Coordinator coord = new Coordinator(user);

      userRepo.save(user);
      coordRepo.save(coord);
    }

    {
      User user =
          new User(
              "Student", Role.STUDENT, "student@example.com", passwordEncoder.encode("hunter2"));
      Student student = new Student(user, StudentType.EXTERNAL, StudentStatus.ACTIVE, "");

      userRepo.save(user);
      studentRepo.save(student);
    }
  }

  @BeforeAll
  void setupEvent() throws Exception {
    Season season = new Season(Semester.SPRING, 9999, SeasonStatus.FINALIZED);

    HackathonEvent event =
        new HackathonEvent(
            "Event",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            EventStatus.FINALIZED,
            season);

    seasonRepo.save(season);
    eventRepo.save(event);

    testEvent = event;
  }

  @BeforeAll
  void setupOngoingEvent() throws Exception {
    Season season = new Season(Semester.SPRING, 9999, SeasonStatus.FINALIZED);

    HackathonEvent event =
        new HackathonEvent(
            "Event",
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            EventStatus.FINALIZED,
            season);

    Round round =
        new Round("Test round", OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), "", event);

    seasonRepo.save(season);
    eventRepo.save(event);
    roundRepo.save(round);

    ongoingEvent = event;
  }

  @Test
  @Order(1)
  void createTeam() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(GlobalConfig.API_BASE + "/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objMapper.writeValueAsString(
                        new CreateTeamRequestDto("Team", "", testEvent.getId())))
                .header("Authorization", "Bearer " + token))
        .andExpectAll(MockMvcResultMatchers.status().isCreated());
  }

  @Test
  @Order(2)
  void approveTeam() throws Exception {
    Team team = teamRepo.findAll().getFirst();
    team.setTeamStatus(TeamStatus.ACTIVE);
    teamRepo.save(team);
  }

  @Test
  @Order(3)
  void submitWorkBadLinks() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE
                        + "/seasons/"
                        + testEvent.getSeason().getId()
                        + "/events/"
                        + testEvent.getId()
                        + "/submit")
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
                    GlobalConfig.API_BASE
                        + "/seasons/"
                        + testEvent.getSeason().getId()
                        + "/events/"
                        + testEvent.getId()
                        + "/submit")
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
                    GlobalConfig.API_BASE
                        + "/seasons/"
                        + ongoingEvent.getSeason().getId()
                        + "/events/"
                        + ongoingEvent.getId()
                        + "/submit")
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
    Team team = teamRepo.findAll().getFirst();

    for (int i = 0; i < 3; i++) {
      User user = new User("Fake student", Role.STUDENT, "a" + i + "@a.b", "");
      Student student = new Student(user, StudentType.EXTERNAL, StudentStatus.ACTIVE, "SE" + i);
      student.setTeam(team);

      userRepo.save(user);
      studentRepo.save(student);
    }

    teamRepo.save(team);
  }

  @Test
  @Order(5)
  void submitWork() throws Exception {
    String token = loginAndGetToken("student@example.com", "hunter2");

    mvc.perform(
            MockMvcRequestBuilders.post(
                    GlobalConfig.API_BASE
                        + "/seasons/"
                        + ongoingEvent.getSeason().getId()
                        + "/events/"
                        + ongoingEvent.getId()
                        + "/submit")
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
