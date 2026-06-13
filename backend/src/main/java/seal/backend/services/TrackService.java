package seal.backend.services;

import java.util.UUID;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;

public interface TrackService {
  CreateTrackResponse createTrack(UUID eventId, CreateTrackRequest request);
}
