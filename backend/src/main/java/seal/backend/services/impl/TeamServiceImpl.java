package seal.backend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.enums.StudentStatus;
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
            .findById(request.seasonId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
    Student leader =
        (Student)
            studentRepository
                .findById(request.leaderId())
                .orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leader not found"));

    if (leader.getStudentStatus() != StudentStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE students are allowed to create team");
    }

    Team team = new Team(request.name(), request.description(), TeamStatus.PENDING, season, leader);

    return teamRepository.save(team);
  }
}
