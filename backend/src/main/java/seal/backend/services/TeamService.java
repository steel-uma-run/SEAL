package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.CreateTeamRequestDto;
import seal.openapi.model.TeamDto;

public interface TeamService {
  TeamDto createTeam(CreateTeamRequestDto request);

  List<TeamDto> getAllTeamsOfEvent(UUID eventId);

  void approveTeam(UUID teamId);

  void inviteToTeam(UUID teamId, UUID studenUuid);
}
