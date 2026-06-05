package seal.backend.services;

import seal.backend.entities.Team;
import seal.backend.requests.CreateTeamRequest;

public interface TeamService {
  Team createTeam(CreateTeamRequest request);
}
