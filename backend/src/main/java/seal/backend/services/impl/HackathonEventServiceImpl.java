package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Round;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Submission;
import seal.backend.entities.Team;
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.enums.StudentStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.services.HackathonEventService;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateEventRequestDto;

@Service
@RequiredArgsConstructor
public class HackathonEventServiceImpl implements HackathonEventService {
  private final HackathonEventRepository hackathonEventRepository;
  private final SubmissionRepository submissionRepository;
  private final SeasonRepository seasonRepository;
  private final StudentRepository studentRepository;
  private final TeamRepository teamRepo;
  private final TrackRepository trackRepo;
  private final RoundRepository roundRepo;

  @Override
  public List<StudentDto> getInterestedParticipants(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    return event.getStudents().stream().map(student -> student.toDto()).toList();
  }

  @Override
  public HackathonEventDto updateEvent(UUID eventId, UpdateEventRequestDto request) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() == EventStatus.FINALIZED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is finalized");
    }

    if (request.name() != null) {
      event.setName(request.name());
    }

    if (request.description() != null) {
      event.setDescription(request.description());
    }

    if (request.prize() != null) {
      event.setPrize(request.prize());
    }

    if (request.registrationDuration() != null) {
      event.setRegistrationDuration(Duration.ofMillis(request.registrationDuration()));
    }

    return hackathonEventRepository.save(event).toDto();
  }

  @Override
  @Transactional
  public void markInterested(UUID eventId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    String currentStudentEmail = auth.getName();
    Student student =
        studentRepository
            .findByEmail(currentStudentEmail)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (student.getStudentStatus() != StudentStatus.ACTIVE) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only active students are allowed to register for events.");
    }

    OffsetDateTime now = OffsetDateTime.now();
    if (event.getRegistrationStartTime() == null
        || now.isAfter(event.getRegistrationStartTime().plus(event.getRegistrationDuration()))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "It is outside of registration timeframe.");
    }

    boolean changed = student.getEvents().add(event);
    if (changed) {
      studentRepository.save(student);
    }
  }

  @Override
  public void finalizeEvent(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() == EventStatus.FINALIZED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Already finalized");
    }

    event.setStatus(EventStatus.FINALIZED);
    event.setRegistrationStartTime(OffsetDateTime.now());

    hackathonEventRepository.save(event);
  }

  @Override
  public HackathonEventDto getEvent(UUID eventId) {
    HackathonEvent result =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    return result.toDto();
  }

  @Override
  public List<HackathonEventDto> getAllEvents(UUID seasonId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean isCoordinator =
        auth != null
            && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("COORDINATOR"));

    List<HackathonEvent> found = new ArrayList<>();
    List<HackathonEventDto> resultList = new ArrayList<>();

    if (isCoordinator) {
      found = hackathonEventRepository.findBySeasonId(seasonId);
    } else {
      found = hackathonEventRepository.findBySeasonIdAndStatus(seasonId, EventStatus.FINALIZED);
    }

    for (HackathonEvent event : found) {
      resultList.add(event.toDto());
    }

    return resultList;
  }

  @Override
  public HackathonEventDto createEvent(CreateEventRequestDto request) {
    Season season =
        seasonRepository
            .findById(request.seasonId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));

    HackathonEvent hackathonEvent =
        new HackathonEvent(
            request.name(),
            request.description(),
            Duration.ofMillis(request.registrationDuration()),
            EventStatus.DRAFT,
            season,
            request.prize());

    return hackathonEventRepository.save(hackathonEvent).toDto();
  }

  @Override
  public List<TeamDto> getAllTeamsOfEvent(UUID eventId) {
    hackathonEventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

    List<Team> teams = teamRepo.findByHackathonEventId(eventId);
    List<TeamDto> resultList = new ArrayList<>();

    for (Team team : teams) {
      resultList.add(team.toDto());
    }

    return resultList;
  }

  @Override
  public List<TrackDto> getAllTracksOfEvent(UUID eventId) {
    List<Track> trackEntities = trackRepo.findByEventId(eventId);
    List<TrackDto> resultList = new ArrayList<>();

    for (Track track : trackEntities) {
      resultList.add(track.toDto());
    }

    return resultList;
  }

  @Override
  public List<TeamDto> getRanking(UUID eventId) {
    return submissionRepository.findAllBySubmitterTeamHackathonEventId(eventId).stream()
        .filter(s -> s.calculateAvgScore() != null)
        .sorted(
            Comparator.comparing(
                    Submission::calculateAvgScore, Comparator.nullsLast(Double::compareTo))
                .reversed())
        .map(Submission::getSubmitterTeam)
        .map(Team::toDto)
        .toList();
  }

  @Transactional
  @Override
  public void advance(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event does not exist."));

    Round activeRound =
        event
            .getActiveRound()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is not ongoing."));

    List<Track> tracks = trackRepo.findByEventId(eventId);
    if (tracks.size() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not have any tracks.");
    }

    // eliminate teams that aren't advanced
    for (Track track : tracks) {
      List<Team> teams = teamRepo.findAllByTrackId(track.getId());
      List<Team> topTeams =
          teams.stream()
              .<Map.Entry<Team, Double>>mapMulti(
                  (team, consumer) -> {
                    List<Submission> submissions =
                        submissionRepository.findAllBySubmitterTeamId(team.getId());
                    if (submissions.size() <= 0) {
                      // team automatically disqualified because they haven't submitted anything
                      return;
                    }

                    Submission latestSubmission = submissions.getLast();
                    consumer.accept(Map.entry(team, latestSubmission.calculateAvgScore()));
                  })
              .sorted(Comparator.comparingDouble(Map.Entry::getValue))
              .collect(Collectors.mapping(Map.Entry::getKey, Collectors.toList()))
              .reversed();

      for (Team team : teams.subList(0, Math.min(2, topTeams.size()))) {
        if (!topTeams.contains(team)) {
          team.setEliminatedAtRound(activeRound);
        }

        teamRepo.save(team);
      }
    }

    // start the next round
    Optional<Round> nextRound =
        event.getRounds().stream().filter(pred -> pred.getActiveTime() == null).findFirst();

    if (nextRound.isPresent()) {
      nextRound.get().setActiveTime(OffsetDateTime.now());
      roundRepo.save(nextRound.get());
    }
  }
}
