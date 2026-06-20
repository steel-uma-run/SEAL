package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.User;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.TeamService;
import seal.openapi.model.CreateTeamRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TeamStatusDto;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
  private final TeamRepository teamRepository;
  private final HackathonEventRepository hackathonEventRepository;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public TeamDto createTeam(CreateTeamRequestDto request) {
    String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

    User currentUser =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not authenticated."));

    Student leader =
        studentRepository
            .findByUser(currentUser)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Only Student accounts can create a team."));
    leader = studentRepository.findById(leader.getId()).orElse(leader);

    HackathonEvent hackathonEvent =
        hackathonEventRepository
            .findById(request.eventId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    if (leader.getStudentStatus() != StudentStatus.ACTIVE) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only ACTIVE students are allowed to create a team.");
    }

    System.out.println(
        "Debug - Current Team ID: "
            + (leader.getTeam() != null ? leader.getTeam().getId() : "NULL"));
    if (leader.getTeam() != null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "This student is already in a team and cannot create another one.");
    }

    Team newTeam = new Team();
    newTeam.setName(request.name());
    newTeam.setDescription(request.description());
    newTeam.setTeamStatus(TeamStatus.PENDING);
    newTeam.setHackathonEvent(hackathonEvent);
    newTeam.setLeader(leader);

    Team savedTeam = teamRepository.save(newTeam);

    leader.setTeam(savedTeam);
    studentRepository.save(leader);

    return new TeamDto(
        savedTeam.getId(),
        savedTeam.getName(),
        TeamStatusDto.valueOf(savedTeam.getTeamStatus().name()),
        new UUID[0],
        leader.getId(),
        null);
  }

  @Override
  public List<TeamDto> getAllTeamsOfEvent(UUID eventId) {
    hackathonEventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    List<Team> teams = teamRepository.findByHackathonEventId(eventId);
    List<TeamDto> resultList = new ArrayList<>();

    for (Team team : teams) {
      TeamDto dto =
          new TeamDto(
              team.getId(),
              team.getName(),
              TeamStatusDto.valueOf(team.getTeamStatus().name()),
              new UUID[0],
              team.getLeader().getId(),
              team.getTrack() != null ? team.getTrack().getId() : null);
      resultList.add(dto);
    }

    return resultList;
  }
}
