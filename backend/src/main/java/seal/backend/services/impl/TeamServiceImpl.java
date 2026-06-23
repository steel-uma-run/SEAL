package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.TeamInvite;
import seal.backend.entities.User;
import seal.backend.enums.InviteStatus;
import seal.backend.enums.Role;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamInviteRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.TeamService;
import seal.openapi.model.CreateTeamRequestPayloadDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TeamStatusDto;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
  private final TeamRepository teamRepository;
  private final HackathonEventRepository hackathonEventRepository;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final TeamInviteRepository inviteRepository;

  @Override
  @Transactional
  public TeamDto createTeam(CreateTeamRequestPayloadDto request) {
    String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

    User currentUser =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not authenticated."));

    Student leader;
    if (currentUser.getRole() == Role.COORDINATOR) {
      if (request.leaderId() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "leader_id is required when creating a team as coordinator.");
      }
      leader =
          studentRepository
              .findById(request.leaderId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND, "Student leader not found."));
    } else {
      leader =
          studentRepository
              .findById(currentUser.getId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.FORBIDDEN, "Only Student accounts can create a team."));
      leader = studentRepository.findById(leader.getId()).orElse(leader);
    }

    HackathonEvent hackathonEvent =
        hackathonEventRepository
            .findById(request.eventId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    if (leader.getStudentStatus() != StudentStatus.ACTIVE) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only ACTIVE students are allowed to create a team.");
    }

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

    List<UUID> memberUuids = new ArrayList<>();
    if (currentUser.getRole() == Role.COORDINATOR && request.memberIds() != null) {
      for (UUID memberId : request.memberIds()) {
        if (memberId.equals(leader.getId())) {
          continue;
        }

        Student member =
            studentRepository
                .findById(memberId)
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Member student not found: " + memberId));

        if (member.getStudentStatus() != StudentStatus.ACTIVE) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Student member is not ACTIVE: " + member.getUser().getFullName());
        }

        if (member.getTeam() != null) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Student member is already in a team: " + member.getUser().getFullName());
        }

        member.setTeam(savedTeam);
        studentRepository.save(member);
        memberUuids.add(memberId);
      }
    }

    return new TeamDto(
        savedTeam.getId(),
        savedTeam.getName(),
        TeamStatusDto.valueOf(savedTeam.getTeamStatus().name()),
        memberUuids.toArray(UUID[]::new),
        leader.getId(),
        null);
  }

  @Override
  public void approveTeam(UUID teamId) {
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team does not exist."));

    if (team.getTeamStatus() != TeamStatus.PENDING) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team is not in PENDING status.");
    }

    team.setTeamStatus(TeamStatus.ACTIVE);
    teamRepository.save(team);
  }

  @Override
  public void inviteToTeam(UUID teamId, UUID studentId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team does not exist."));

    if (team.getTeamStatus() != TeamStatus.ACTIVE) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Team is not allowed to send invites.");
    }

    Student actor =
        studentRepository
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    if (!actor.isTeamLeaderOf(team)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    Student invitee =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitee does not exist."));

    // This should never happen because the client should never know the UUID of an unapproved
    // account in the first place. Nevertheless, checking can't hurt.
    if (invitee.getStudentStatus() != StudentStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitee is not yet active.");
    }

    // TODO: check if student is interested in this event
    // needs #135, #136
    TeamInvite invite =
        new TeamInvite(
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(3),
            InviteStatus.PENDING,
            team,
            invitee);

    inviteRepository.save(invite);
  }
}
