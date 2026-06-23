package seal.backend;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import seal.backend.entities.AuditLog;
import seal.backend.entities.audit.AccountApprovedLog;
import seal.backend.entities.audit.AccountBannedLog;
import seal.backend.repositories.AuditLogRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogTests {
  @Autowired private AuditLogRepository<AuditLog> repo;
  @Autowired private CreateUtils createUtils;

  @Test
  @Transactional
  void smokeTest1() throws Exception {
    repo.save(
        AccountApprovedLog.builder()
            .actionTime(OffsetDateTime.now())
            .actor(createUtils.createCoord())
            .approvedStudent(createUtils.createStudent())
            .build());
  }

  @Test
  @Transactional
  void smokeTest2() throws Exception {
    repo.save(
        AccountBannedLog.builder()
            .actionTime(OffsetDateTime.now())
            .actor(createUtils.createCoord())
            .banTarget(createUtils.createStudent())
            .banReason("thich")
            .build());
  }
}
