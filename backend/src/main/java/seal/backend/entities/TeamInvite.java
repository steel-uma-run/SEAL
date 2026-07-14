package seal.backend.entities;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import seal.backend.enums.InviteStatus;
import seal.openapi.model.TeamInviteDto;
import seal.openapi.model.TeamInviteStatusDto;

@Entity
@Table(name = "team_invites")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class TeamInvite {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime sentAt;

  @Column(columnDefinition = "timestamptz", nullable = false)
  @Nonnull
  private OffsetDateTime expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Nonnull
  private InviteStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inviting_team_id", nullable = false)
  @Nonnull
  private Team invitingTeam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invitee_id", nullable = false)
  @Nonnull
  private Student invitee;

  public TeamInviteDto toDto() {
    return new TeamInviteDto(
        getId(),
        getSentAt(),
        getExpiresAt(),
        TeamInviteStatusDto.fromValue(getStatus().name()),
        getInvitingTeam().getId());
  }
}
