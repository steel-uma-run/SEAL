package seal.backend.services;

import java.util.List;
import seal.backend.entities.HackathonEvent;
import seal.backend.requests.CreateEventRequest;

public interface HackathonEventService {
  public List<HackathonEvent> getAllEvents();

  public HackathonEvent createEvent(CreateEventRequest request);
}
