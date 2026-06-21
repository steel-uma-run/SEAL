package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Lecturer;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {}
