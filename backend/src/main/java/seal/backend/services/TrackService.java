package seal.backend.services;

import java.util.List;
import java.util.UUID;
import seal.backend.entities.Track;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;

public interface TrackService {
  CreateTrackResponse createTrack(UUID eventId, CreateTrackRequest request);

  List<Track> getTracksByEventId(UUID eventId);
}
