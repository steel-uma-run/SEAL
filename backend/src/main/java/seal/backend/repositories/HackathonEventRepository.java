package seal.backend.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.HackathonEvent;

public interface HackathonEventRepository extends JpaRepository<HackathonEvent, UUID> {}
