package seal.backend.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.requests.CreateTeamRequest;
import seal.backend.services.TeamService;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
  private final TeamRepository teamRepository;
  private final HackathonEventRepository hackathonEventRepository;
  private final StudentRepository studentRepository;

  @Override
  public Team createTeam(CreateTeamRequest request) {
    HackathonEvent hackathonEvent =
        hackathonEventRepository
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

    Team team =
        new Team(request.name(), request.description(), TeamStatus.PENDING, hackathonEvent, leader);

    return teamRepository.save(team);
  }
}
