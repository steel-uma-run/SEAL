package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Team;
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.services.TrackService;
import seal.openapi.model.CreateTrackRequestDto;
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

    return track.toDto();
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

    return savedTrack.toDto();
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

    // 1. Clear old mentors if new list is provided
    if (request.mentorIds() != null) {
      track.getMentors().clear();
    }

    // 2. Clear old judges if new list is provided
    if (request.judgeIds() != null) {
      track.getJudges().clear();
    }

    // Save the track state (cleared lists) so constraints are evaluated against the clean state.
    // track = trackRepository.save(track);

    // 3. Assign new mentors
    if (request.mentorIds() != null) {
      for (UUID id : request.mentorIds()) {
        track = assignMentor(track, id);
      }
    }

    // 4. Assign new judges
    if (request.judgeIds() != null) {
      for (UUID id : request.judgeIds()) {
        track = assignJudge(track, id);
      }
    }

    // 5. Clear and assign new teams
    if (request.teamIds() != null) {
      List<Team> currentTeams = teamRepository.findByHackathonEventId(track.getEvent().getId());
      for (Team team : currentTeams) {
        if (team.getTrack() != null && team.getTrack().getId().equals(trackId)) {
          team.setTrack(null);
          teamRepository.save(team);
        }
      }

      for (UUID id : request.teamIds()) {
        assignTeam(track, id);
      }
    }

    Track savedTrack = trackRepository.save(track);
    return savedTrack.toDto();
  }

  @Override
  @Transactional
  public void deleteTrack(UUID trackId) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found."));

    if (track.getEvent().getStatus() != EventStatus.DRAFT) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete track. Event is not in DRAFT status.");
    }

    track.getMentors().clear();
    track.getJudges().clear();
    trackRepository.save(track);

    trackRepository.delete(track);
  }

  private Track assignMentor(Track track, UUID mentorId) {
    Lecturer mentor =
        lecturerRepository
            .findById(mentorId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found."));

    if (track.getJudges().contains(mentor)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "This lecturer has been assigned as a judge for this track and cannot be a mentor");
    }

    if (track.getMentors().contains(mentor)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "This lecturer is already a mentor for this track.");
    }

    if (track.getMentors().size() >= 3) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot assign more mentors. This track already has the maximum of 3 mentors.");
    }

    List<Track> allTracksInEvent = trackRepository.findByEventId(track.getEvent().getId());
    for (Track trackInEvent : allTracksInEvent) {
      if (trackInEvent.getId().equals(track.getId())) continue;

      if (trackInEvent.getMentors().contains(mentor)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "This lecturer is already assigned to another track within the same event.");
      }
    }

    track.getMentors().add(mentor);
    return trackRepository.save(track);
  }

  private Track assignJudge(Track track, UUID judgeId) {
    Lecturer judge =
        lecturerRepository
            .findById(judgeId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found."));

    if (track.getMentors().contains(judge)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "This lecturer has been assigned as a mentor for this track and cannot be a judge");
    }

    if (track.getJudges().contains(judge)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "This lecturer is already assigned to this track.");
    }

    if (track.getJudges().size() >= 3) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot assign more judges. This track already has the maximum of 3 judges.");
    }

    List<Track> allTracksInEvent = trackRepository.findByEventId(track.getEvent().getId());
    for (Track trackInEvent : allTracksInEvent) {
      if (trackInEvent.getId().equals(track.getId())) continue;

      if (trackInEvent.getJudges().contains(judge)) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "This lecturer is already assigned to another track within the same event.");
      }
    }

    track.getJudges().add(judge);
    return trackRepository.save(track);
  }

  private void assignTeam(Track track, UUID teamId) {
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found."));

    if (!team.getHackathonEvent().getId().equals(track.getEvent().getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The team and track must belong to the same event.");
    }

    team.setTrack(track);
    teamRepository.save(team);
  }
}
