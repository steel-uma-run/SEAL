package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.CreateTeamRequestPayloadDto;
import seal.openapi.model.CreateTeamTemplateRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TeamTemplateDto;

public interface TeamService {
  TeamDto createTeam(CreateTeamRequestPayloadDto request);

  void approveTeam(UUID teamId);

  void inviteToTeam(UUID teamId, UUID studenUuid);

  TeamDto getTeamInfo(UUID teamId);

  TeamTemplateDto[] getAllTemplates();

  TeamTemplateDto createTemplate(CreateTeamTemplateRequestDto request);

  TeamTemplateDto updateTemplate(UUID templateId, CreateTeamTemplateRequestDto request);

  void deleteTemplate(UUID templateId);
}
