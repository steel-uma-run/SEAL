package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.Track;

public interface TrackRepository extends JpaRepository<Track, UUID> {
  List<Track> findByEventId(UUID eventId);
}
