package seal.backend.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.count() == 0) {
      log.info("Database is empty. Seeding accounts per role...");

      String defaultPassword = passwordEncoder.encode("123456");

      // Student Account
      User student =
          new User("Student Nguyen", Role.STUDENT, "student@fpt.edu.vn", defaultPassword);
      userRepository.save(student);

      // Judge (Lecturer) Account
      User judge = new User("Lecturer Tran", Role.LECTURER, "lecturer@fpt.edu.vn", defaultPassword);
      userRepository.save(judge);

      // Coordinator Account
      User coordinator =
          new User("Coordinator Le", Role.COORDINATOR, "coordinator@fpt.edu.vn", defaultPassword);
      userRepository.save(coordinator);

      // SysAdmin Account
      User sysadmin = new User("System Admin", Role.SYSADMIN, "admin@fpt.edu.vn", defaultPassword);
      userRepository.save(sysadmin);

      log.info("Seeding completed successfully! Mật khẩu mặc định cho tất cả tài khoản là: 123456");
    } else {
      log.info("Database already contains data. Seeding skipped.");
    }
  }
}
