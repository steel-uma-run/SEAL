package seal.backend;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Coordinator;
import seal.backend.entities.Student;
import seal.backend.entities.User;
import seal.backend.entities.audit.AccountApprovedLog;
import seal.backend.entities.audit.AccountBannedLog;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.repositories.CoordinatorRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogTests {
  @Autowired private AuditLogRepository<AuditLog> repo;
  @Autowired private UserRepository userRepo;
  @Autowired private CoordinatorRepository coordRepo;
  @Autowired private StudentRepository studentRepo;

  Coordinator createCoord() {
    User user = new User("NHC", Role.COORDINATOR, "", "");
    Coordinator coord = new Coordinator(userRepo.save(user));

    return coordRepo.save(coord);
  }

  Student createStudent() {
    User user = new User("NHC", Role.COORDINATOR, "a@b.c", "");
    Student student =
        new Student(userRepo.save(user), StudentType.EXTERNAL, StudentStatus.ACTIVE, "S");

    return studentRepo.save(student);
  }

  @Test
  @Transactional
  void smokeTest1() throws Exception {
    repo.save(
        AccountApprovedLog.builder()
            .actionTime(OffsetDateTime.now())
            .actor(createCoord())
            .approvedStudent(createStudent())
            .build());
  }

  @Test
  @Transactional
  void smokeTest2() throws Exception {
    repo.save(
        AccountBannedLog.builder()
            .actionTime(OffsetDateTime.now())
            .actor(createCoord())
            .banTarget(createStudent())
            .banReason("thich")
            .build());
  }
}
