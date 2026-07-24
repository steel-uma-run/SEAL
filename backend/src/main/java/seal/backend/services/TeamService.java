package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.CreateTeamRequestPayloadDto;
import seal.openapi.model.TeamDto;

public interface TeamService {
  TeamDto createTeam(CreateTeamRequestPayloadDto request);

  void approveTeam(UUID teamId);

  void inviteToTeam(UUID teamId, UUID studenUuid);

  TeamDto getTeamInfo(UUID teamId);
}
