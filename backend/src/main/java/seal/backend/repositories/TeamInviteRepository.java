package seal.backend.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import seal.backend.entities.TeamInvite;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
  public List<TeamInvite> findAllByInviteeId(UUID inviteeId);
}
