package seal.backend.services.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Season;
import seal.backend.entities.Track;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.requests.CreateTrackRequest;
import seal.backend.responses.CreateTrackResponse;
import seal.backend.services.TrackService;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {
  private final TrackRepository trackRepository;
  private final SeasonRepository seasonRepository;

  @Override
  public CreateTrackResponse createTrack(UUID seasonId, CreateTrackRequest request) {
    Season season =
        seasonRepository
            .findById(seasonId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found."));
    Track track =
        Track.builder()
            .name(request.name())
            .description(request.description())
            .season(season)
            .build();
    Track savedTrack = trackRepository.save(track);

    return CreateTrackResponse.builder()
        .id(savedTrack.getId())
        .name(savedTrack.getName())
        .description(savedTrack.getDescription())
        .seasonId(season.getId())
        .build();
  }
}
