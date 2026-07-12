package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.AssignJudgeRequestDto;
import seal.openapi.model.AssignMentorRequestDto;
import seal.openapi.model.AssignTeamRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateTrackRequestDto;

public interface TrackService {

  TrackDto getTrack(UUID trackId);

  TrackDto createTrack(CreateTrackRequestDto request, UUID eventId);

  TrackDto updateTrack(UUID trackId, UpdateTrackRequestDto request);

  TrackDto assignMentor(UUID trackId, AssignMentorRequestDto request);

  TrackDto assignJudge(UUID trackId, AssignJudgeRequestDto request);

  TeamDto assignTeam(UUID trackId, AssignTeamRequestDto request);
}
