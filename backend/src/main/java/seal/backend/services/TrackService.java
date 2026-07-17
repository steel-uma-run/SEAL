package seal.backend.services;

import java.util.UUID;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateTrackRequestDto;

public interface TrackService {
  TrackDto getTrack(UUID trackId);

  TrackDto createTrack(CreateTrackRequestDto request, UUID eventId);

  TrackDto updateTrack(UUID trackId, UpdateTrackRequestDto request);
}
