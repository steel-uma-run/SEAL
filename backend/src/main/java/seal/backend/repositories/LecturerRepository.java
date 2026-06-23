package seal.backend.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Lecturer;
import seal.backend.entities.User;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {
  Optional<Lecturer> findByUserEmail(String email);

  Optional<Lecturer> findByUser(User user);
}
