package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.AssignMentorRequestDto;
import seal.openapi.model.AssignTeamRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateTrackRequestDto;

public interface TrackService {

  List<TrackDto> getAllTracksOfEvent(UUID seasonId, UUID eventId);

  TrackDto getTrack(UUID seasonId, UUID eventId, UUID trackId);

  TrackDto createTrack(CreateTrackRequestDto request, UUID seasonId, UUID eventId);

  TrackDto updateTrack(UUID seasonId, UUID eventId, UUID trackId, UpdateTrackRequestDto request);

  TrackDto assignMentor(UUID seasonId, UUID eventId, UUID trackId, AssignMentorRequestDto request);

  TeamDto assignTeam(UUID seasonId, UUID eventId, UUID trackId, AssignTeamRequestDto request);
}
