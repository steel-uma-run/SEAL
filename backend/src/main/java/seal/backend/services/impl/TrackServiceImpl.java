package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
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
import seal.openapi.model.UpdateTrackRequestDto;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {
  private final TrackRepository trackRepository;
  private final HackathonEventRepository eventRepo;
  private final LecturerRepository lecturerRepository;
  private final TeamRepository teamRepository;

  @Override
  public TrackDto getTrack(UUID trackId) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Track does not exist."));

    return new TrackDto(
        track.getId(),
        track.getName(),
        track.getDescription(),
        track.getEvent().getId(),
        track.getMentors().stream().map(Lecturer::getId).toArray(UUID[]::new));
  }

  @Override
  @Transactional
  public TrackDto createTrack(CreateTrackRequestDto request, UUID eventId) {
    HackathonEvent hackathonEvent =
        eventRepo
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not exist."));

    Track newTrack = new Track(request.name(), request.description(), hackathonEvent);
    Track savedTrack = trackRepository.save(newTrack);

    return new TrackDto(
        savedTrack.getId(),
        savedTrack.getName(),
        savedTrack.getDescription(),
        hackathonEvent.getId(),
        savedTrack.getMentors().stream().map(Lecturer::getId).toArray(UUID[]::new));
  }

  @Override
  @Transactional
  public TrackDto updateTrack(UUID trackId, UpdateTrackRequestDto request) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track does not exist."));

    if (request.name() != null) {
      track.setName(request.name());
    }

    if (request.description() != null) {
      track.setDescription(request.description());
    }

    Track savedTrack = trackRepository.save(track);

    return new TrackDto(
        savedTrack.getId(),
        savedTrack.getName(),
        savedTrack.getDescription(),
        savedTrack.getEvent().getId(),
        savedTrack.getMentors().stream().map(Lecturer::getId).toArray(UUID[]::new));
  }

  @Override
  @Transactional
  public TrackDto assignMentor(UUID trackId, AssignMentorRequestDto request) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Track does not exist."));

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

    if (track.getMentors().size() >= 3) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot assign more mentors. This track already has the maximum of 3 mentors.");
    }

    List<Track> allTracksInEvent = trackRepository.findByEventId(track.getEvent().getId());
    for (Track currentTrack : allTracksInEvent) {
      if (currentTrack.getMentors().contains(mentor)) {
        if (currentTrack.getId().equals(trackId)) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "This lecturer is already a mentor for this track.");
        } else {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "This lecturer is already assigned to another track within the same event.");
        }
      }
    }

    track.getMentors().add(mentor);
    Track savedTrack = trackRepository.save(track);

    return new TrackDto(
        savedTrack.getId(),
        savedTrack.getName(),
        savedTrack.getDescription(),
        savedTrack.getEvent().getId(),
        savedTrack.getMentors().stream().map(Lecturer::getId).toArray(UUID[]::new));
  }

  @Override
  @Transactional
  public TeamDto assignTeam(UUID trackId, AssignTeamRequestDto request) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Track does not exist."));
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
