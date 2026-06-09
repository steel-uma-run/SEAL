package seal.backend.services;

import java.util.UUID;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;

public interface TrackService {
  CreateTrackResponse createTrack(UUID seasonId, CreateTrackRequest request);
}
