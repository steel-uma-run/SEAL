package seal.backend.services.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.TeamInvite;
import seal.backend.enums.InviteStatus;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TeamInviteRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.services.InviteService;
import seal.openapi.model.TeamInviteDto;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {
  private final TeamInviteRepository inviteRepository;
  private final StudentRepository studentRepository;
  private final TeamRepository teamRepository;

  @Override
  public List<TeamInviteDto> getAllInvites() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Student actor =
        studentRepository
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    List<TeamInvite> invites = inviteRepository.findAllByInviteeId(actor.getId());

    return invites.stream().map(TeamInvite::toDto).toList();
  }

  @Override
  @jakarta.transaction.Transactional
  public void acceptInvite(UUID inviteId) {
    TeamInvite invite =
        inviteRepository
            .findById(inviteId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite does not exist."));

    if (invite.getStatus() != InviteStatus.PENDING) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite is already used.");
    }

    Team team = invite.getInvitingTeam();

    OffsetDateTime now = OffsetDateTime.now();
    if (now.isBefore(team.getHackathonEvent().getStartTime())
        || now.isAfter(team.getHackathonEvent().getEndTime())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "This event is currently not open for team registration.");
    }

    if (invite.getInvitee().getTeams().stream()
        .anyMatch(t -> t.getHackathonEvent().getId().equals(team.getHackathonEvent().getId()))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "You are already in a team for this event.");
    }

    if (team.getMembers().size() >= 5) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Team is already full.");
    }

    invite.setStatus(InviteStatus.ACCEPTED);
    invite.getInvitee().getTeams().add(team);
    team.getMembers().add(invite.getInvitee());

    inviteRepository.save(invite);
    studentRepository.save(invite.getInvitee());
    teamRepository.save(team);
  }

  @Override
  public void declineInvite(UUID inviteId) {
    TeamInvite invite =
        inviteRepository
            .findById(inviteId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite does not exist."));

    if (invite.getStatus() != InviteStatus.PENDING) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite is already used.");
    }

    invite.setStatus(InviteStatus.DECLINED);
    inviteRepository.save(invite);
  }
}
