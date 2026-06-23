package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.UpdateEventRequestDto;

public interface HackathonEventService {

  List<StudentDto> getInterestedParticipants(UUID eventId);

  HackathonEventDto updateEvent(UUID eventId, UpdateEventRequestDto request);

  List<HackathonEventDto> getAllEvents();

  HackathonEventDto getEvent(UUID seasonId, UUID eventId);

  HackathonEventDto createEvent(CreateEventRequestDto request);

  void finalizeEvent(UUID seasonId, UUID eventId);

  void markInterested(UUID eventId);
}
