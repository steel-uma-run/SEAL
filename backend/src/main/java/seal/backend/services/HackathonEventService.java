package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.SubmissionRankDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateEventRequestDto;

public interface HackathonEventService {
  List<StudentDto> getInterestedParticipants(UUID eventId);

  List<TrackDto> getAllTracksOfEvent(UUID eventId);

  List<TeamDto> getAllTeamsOfEvent(UUID eventId);

  HackathonEventDto updateEvent(UUID eventId, UpdateEventRequestDto request);

  List<HackathonEventDto> getAllEvents(UUID seasonId);

  HackathonEventDto getEvent(UUID eventId);

  HackathonEventDto createEvent(CreateEventRequestDto request);

  void finalizeEvent(UUID eventId);

  void markInterested(UUID eventId);

  List<SubmissionRankDto> getRanking(UUID eventId);
}
