package seal.backend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.requests.CreateTeamRequest;
import seal.backend.services.TeamService;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
  private final TeamRepository teamRepository;
  private final SeasonRepository seasonRepository;
  private final StudentRepository studentRepository;

  @Override
  public Team createTeam(CreateTeamRequest request) {
    Season season =
        seasonRepository
            .findById(request.getSeasonId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
    Student leader =
        (Student)
            studentRepository
                .findById(request.getLeaderId())
                .orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

    Team team = new Team();
    team.setName(request.getName());
    team.setDescription(request.getDescription());
    team.setTeamStatus(TeamStatus.PENDING);
    team.setSeason(season);
    team.setLeader(leader);

    return teamRepository.save(team);
  }
}
