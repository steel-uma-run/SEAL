package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.TeamInviteDto;

public interface InviteService {
  List<TeamInviteDto> getAllInvites();

  void acceptInvite(UUID inviteId);

  void declineInvite(UUID inviteId);
}
