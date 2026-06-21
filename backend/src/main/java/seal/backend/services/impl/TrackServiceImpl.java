package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.services.TrackService;
import seal.openapi.model.AssignMentorRequestDto;
import seal.openapi.model.AssignTeamRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TeamStatusDto;
import seal.openapi.model.TrackDto;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {
  private final TrackRepository trackRepository;
  private final HackathonEventRepository hackathonEventRepository;
  private final LecturerRepository lecturerRepository;
  private final TeamRepository teamRepository;

  @Override
  public List<TrackDto> getAllTracksOfEvent(UUID eventId) {
    hackathonEventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    List<Track> trackEntities = trackRepository.findByEventId(eventId);
    List<TrackDto> resultList = new ArrayList<>();

    for (Track track : trackEntities) {
      TrackDto dto =
          new TrackDto(
              track.getId(),
              track.getName(),
              track.getDescription(),
              track.getEvent().getId(),
              track.getMentor() != null ? track.getMentor().getId() : null);
      resultList.add(dto);
    }

    return resultList;
  }

  @Override
  public TrackDto getTrack(UUID trackId) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track does not exist"));

    return new TrackDto(
        track.getId(),
        track.getName(),
        track.getDescription(),
        track.getEvent().getId(),
        track.getMentor() != null ? track.getMentor().getId() : null);
  }

  @Override
  @Transactional
  public TrackDto createTrack(CreateTrackRequestDto request, UUID eventId) {
    HackathonEvent hackathonEvent =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    Track newTrack = new Track(request.name(), request.description(), hackathonEvent);
    Track savedTrack = trackRepository.save(newTrack);

    return new TrackDto(
        savedTrack.getId(),
        savedTrack.getName(),
        savedTrack.getDescription(),
        hackathonEvent.getId(),
        savedTrack.getMentor() != null ? savedTrack.getMentor().getId() : null);
  }

  @Override
  @Transactional
  public TrackDto assignMentor(UUID trackId, AssignMentorRequestDto request) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found."));

    Lecturer mentor =
        lecturerRepository
            .findById(request.mentorId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found."));

    if (track.getJudges().contains(mentor)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "This lecturer has been assigned as a judge for this track and cannot be a mentor");
    }

    track.setMentor(mentor);
    Track savedTrack = trackRepository.save(track);

    return new TrackDto(
        savedTrack.getId(),
        savedTrack.getName(),
        savedTrack.getDescription(),
        savedTrack.getEvent().getId(),
        savedTrack.getMentor() != null ? savedTrack.getMentor().getId() : null);
  }

  @Override
  @Transactional
  public TeamDto assignTeam(UUID trackId, AssignTeamRequestDto request) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found."));
    HackathonEvent event = track.getEvent();

    Team team =
        teamRepository
            .findById(request.teamId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found."));

    if (!team.getHackathonEvent().getId().equals(event.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Team and Track do not belong to the same Hackathon Event.");
    }

    if (team.getTeamStatus() != TeamStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team has not been approved yet.");
    }

    if (event.getStatus() != EventStatus.FINALIZED) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot assign team. The event is not finalized yet.");
    }

    if (event.getStartTime().isBefore(OffsetDateTime.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot assign team. The event has already started.");
    }

    team.setTrack(track);
    Team savedTeam = teamRepository.save(team);

    UUID[] memberIds = savedTeam.getMembers().stream().map(Student::getId).toArray(UUID[]::new);

    return new TeamDto(
        savedTeam.getId(),
        savedTeam.getName(),
        TeamStatusDto.APPROVED,
        memberIds,
        savedTeam.getLeader().getId(),
        savedTeam.getTrack().getId());
  }
}
