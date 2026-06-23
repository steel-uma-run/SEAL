package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;

public interface HackathonEventService {
  List<HackathonEventDto> getAllEvents();

  HackathonEventDto getEvent(UUID seasonId, UUID eventId);

  HackathonEventDto createEvent(CreateEventRequestDto request);
}
