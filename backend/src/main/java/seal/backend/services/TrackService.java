package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.AssignTeamRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;

public interface TrackService {
  List<TrackDto> getAllTracksOfEvent(UUID eventId);

  TrackDto getTrack(UUID trackId);

  TrackDto createTrack(CreateTrackRequestDto request, UUID eventId);

  TrackDto assignMentor(UUID trackId, seal.openapi.model.AssignMentorRequestDto request);

  TeamDto assignTeam(UUID trackId, AssignTeamRequestDto request);
}
