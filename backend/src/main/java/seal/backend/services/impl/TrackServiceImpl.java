package seal.backend.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Track;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;
import seal.backend.services.TrackService;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {
  private final TrackRepository trackRepository;
  private final HackathonEventRepository hackathonEventRepository;

  @Override
  public CreateTrackResponse createTrack(UUID seasonId, CreateTrackRequest request) {
    HackathonEvent hackathonEvent =
        hackathonEventRepository
            .findById(seasonId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found."));
    Track track =
        Track.builder()
            .name(request.name())
            .description(request.description())
            .hackathonEvent(hackathonEvent)
            .build();
    Track savedTrack = trackRepository.save(track);

    return CreateTrackResponse.builder()
        .id(savedTrack.getId())
        .name(savedTrack.getName())
        .description(savedTrack.getDescription())
        .eventId(hackathonEvent.getId())
        .build();
  }
}
