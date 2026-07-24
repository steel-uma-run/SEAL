package seal.backend;

import java.time.OffsetDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import seal.backend.entities.Coordinator;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Round;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.TemplatedCriteria;
import seal.backend.enums.EventStatus;
import seal.backend.enums.Role;
import seal.backend.enums.Semester;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.repositories.CoordinatorRepository;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TemplatedCriteriaRepository;

@Service
public class CreateUtils {
  @Autowired private CoordinatorRepository coordRepo;
  @Autowired private StudentRepository studentRepo;
  @Autowired private SeasonRepository seasonRepo;
  @Autowired private HackathonEventRepository eventRepo;
  @Autowired private RoundRepository roundRepo;
  @Autowired private TemplatedCriteriaRepository templatedCriteriaRepo;
  @Autowired private CriteriaTemplateRepository criteriaTemplateRepo;

  public final String randomString(int len) {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    Random random = new Random();

    String generatedString =
        random
            .ints(leftLimit, rightLimit + 1)
            .limit(len)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    return generatedString;
  }

  public final Coordinator createCoord() {
    Coordinator coord =
        Coordinator.builder()
            .email(randomString(12))
            .fullName(randomString(12))
            .passwordHash(randomString(32))
            .role(Role.COORDINATOR)
            .build();

    return coordRepo.save(coord);
  }

  public final Student createStudent() {
    Student student =
        Student.builder()
            .email(randomString(10))
            .fullName(randomString(10))
            .passwordHash(randomString(32))
            .role(Role.STUDENT)
            .schoolName(randomString(32))
            .studentId(randomString(6))
            .studentStatus(StudentStatus.PENDING)
            .studentType(StudentType.EXTERNAL)
            .build();

    return studentRepo.save(student);
  }

  public final HackathonEvent createFinalizedEvent() {
    Season season = new Season(Semester.SPRING, 9999);

    HackathonEvent event =
        new HackathonEvent(
            randomString(20),
            randomString(50),
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1),
            EventStatus.FINALIZED,
            season,
            "");

    seasonRepo.save(season);
    return eventRepo.save(event);
  }

  public final HackathonEvent createOngoingEvent() {
    HackathonEvent event = createFinalizedEvent();

    Round round =
        new Round(
            randomString(50),
            OffsetDateTime.now().minusDays(1),
            OffsetDateTime.now().plusDays(1),
            randomString(50),
            event);

    event.getRounds().add(round);
    roundRepo.save(round);

    return event;
  }

  public final CriteriaTemplate createCriteriaTemplate() {
    CriteriaTemplate template =
        criteriaTemplateRepo.save(new CriteriaTemplate(randomString(10), randomString(50)));

    for (int i = 0; i < 5; i++) {
      TemplatedCriteria crit =
          new TemplatedCriteria(randomString(10), randomString(15), 20, template);
      templatedCriteriaRepo.save(crit);
    }

    return criteriaTemplateRepo.save(template);
  }
}
