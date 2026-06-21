package seal.backend.services.impl;

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
import seal.backend.services.InviteService;
import seal.openapi.model.TeamInviteDto;
import seal.openapi.model.TeamInviteStatusDto;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {
  private final TeamInviteRepository inviteRepository;
  private final StudentRepository studentRepository;

  @Override
  public List<TeamInviteDto> getAllInvites() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Student actor =
        studentRepository
            .findByUserEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    List<TeamInvite> invites = inviteRepository.findAllByInviteeId(actor.getId());

    return invites.stream()
        .map(
            (inviteEntity) ->
                new TeamInviteDto(
                    inviteEntity.getId(),
                    inviteEntity.getSentAt(),
                    inviteEntity.getExpiresAt(),
                    TeamInviteStatusDto.fromValue(inviteEntity.getStatus().name()),
                    inviteEntity.getInvitingTeam().getId()))
        .toList();
  }

  @Override
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
    if (team.getMembers().size() >= 5) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Team is already full.");
    }

    invite.setStatus(InviteStatus.ACCEPTED);
    invite.getInvitee().setTeam(team);

    inviteRepository.save(invite);
    studentRepository.save(invite.getInvitee());
  }
}
