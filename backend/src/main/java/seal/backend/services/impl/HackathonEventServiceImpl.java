package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Submission;
import seal.backend.entities.Team;
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.enums.StudentStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.services.HackathonEventService;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.SubmissionRankDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateEventRequestDto;

@Service
@RequiredArgsConstructor
public class HackathonEventServiceImpl implements HackathonEventService {
  private final HackathonEventRepository hackathonEventRepository;
  private final SeasonRepository seasonRepository;
  private final StudentRepository studentRepository;
  private final SubmissionRepository submissionRepository;
  private final TeamRepository teamRepo;
  private final TrackRepository trackRepo;

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

    if (request.price() != null) {
      event.setPrice(request.price());
    }

    if (request.endTime() != null) {
      if (event.getStartTime() != null && !request.endTime().isAfter(event.getStartTime())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "End time must be after start time");
      }
      event.setEndTime(request.endTime());
    }

    if (request.startTime() != null) {
      if (event.getEndTime() != null && !event.getEndTime().isAfter(request.startTime())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "End time must be after start time");
      }
      event.setStartTime(request.startTime());
    }

    hackathonEventRepository.save(event);

    return event.toDto();
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
    if (now.isBefore(event.getStartTime()) || now.isAfter(event.getEndTime())) {
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
    if (request.endTime().isEqual(request.startTime())
        || request.endTime().isBefore(request.startTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "End time must be after start time");
    }

    Season season =
        seasonRepository
            .findById(request.seasonId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));

    HackathonEvent hackathonEvent =
        new HackathonEvent(
            request.name(),
            request.description(),
            request.startTime(),
            request.endTime(),
            EventStatus.DRAFT,
            season,
            request.price());

    hackathonEventRepository.save(hackathonEvent);

    return hackathonEvent.toDto();
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
  @Transactional
  public List<SubmissionRankDto> getRanking(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() != EventStatus.FINALIZED) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Ranking is only available for finalized events.");
    }

    List<Submission> submissions = submissionRepository.findAllByEventId(eventId);

    List<Map.Entry<Submission, Double>> sorted =
        submissions.stream()
            .map(submission -> Map.entry(submission, submission.calculateTotalScore()))
            .filter(entry -> entry.getValue() != null)
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(10)
            .toList();

    List<SubmissionRankDto> result = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      Map.Entry<Submission, Double> entry = sorted.get(i);
      result.add(new SubmissionRankDto(i + 1, entry.getKey().toDto(), entry.getValue()));
    }

    return result;
  }
}
