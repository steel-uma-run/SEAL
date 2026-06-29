package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.services.HackathonEventService;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.HackathonEventStatusDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.UpdateEventRequestDto;
import seal.openapi.model.UserRoleDto;

@Service
@RequiredArgsConstructor
public class HackathonEventServiceImpl implements HackathonEventService {
  private final HackathonEventRepository hackathonEventRepository;
  private final SeasonRepository seasonRepository;
  private final StudentRepository studentRepository;
  private final TrackRepository trackRepository;

  @Override
  public List<StudentDto> getInterestedParticipants(UUID seasonId, UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    return event.getStudents().stream()
        .map(
            student -> {
              UUID[] interested =
                  student.getEvents().stream().map(HackathonEvent::getId).toArray(UUID[]::new);

              return new StudentDto(
                  student.getId(),
                  student.getUser().getEmail(),
                  student.getUser().getFullName(),
                  UserRoleDto.fromValue(student.getUser().getRole().name()),
                  student.getStudentId(),
                  student.isExternal(),
                  null,
                  student.getTeam() != null ? student.getTeam().getId() : null,
                  interested);
            })
        .toList();
  }

  @Override
  public HackathonEventDto updateEvent(UUID seasonId, UUID eventId, UpdateEventRequestDto request) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    if (event.getStatus() == EventStatus.FINALIZED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is finalized");
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

    return new HackathonEventDto(
        event.getId(),
        event.getName(),
        event.getDescription(),
        HackathonEventStatusDto.fromValue(event.getStatus().name()),
        event.getStartTime(),
        event.getEndTime(),
        event.getSeason().getId());
  }

  @Override
  @Transactional
  public void markInterested(UUID seasonId, UUID eventId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    String currentStudentEmail = auth.getName();
    Student student =
        studentRepository
            .findByUserEmail(currentStudentEmail)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    boolean changed = student.getEvents().add(event);
    if (changed) {
      studentRepository.save(student);
    }
  }

  @Override
  public void finalizeEvent(UUID seasonId, UUID eventId) {
    seasonRepository
        .findById(seasonId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
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
  public HackathonEventDto getEvent(UUID seasonId, UUID eventId) {
    seasonRepository
        .findById(seasonId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Season not found"));
    hackathonEventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    Optional<HackathonEvent> result = hackathonEventRepository.findById(eventId);

    HackathonEventDto dto =
        new HackathonEventDto(
            result.get().getId(),
            result.get().getName(),
            result.get().getDescription(),
            HackathonEventStatusDto.fromValue(result.get().getStatus().name()),
            result.get().getStartTime(),
            result.get().getEndTime(),
            result.get().getSeason().getId());

    return dto;
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
      HackathonEventDto dto =
          new HackathonEventDto(
              event.getId(),
              event.getName(),
              event.getDescription(),
              HackathonEventStatusDto.fromValue(event.getStatus().name()),
              event.getStartTime(),
              event.getEndTime(),
              event.getSeason().getId());
      resultList.add(dto);
    }

    return resultList;
  }

  @Override
  @Transactional
  public HackathonEventDto createEvent(CreateEventRequestDto request) {
    if (request.endTime().isEqual(request.startTime())
        || request.endTime().isBefore(request.startTime())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "End time must be after start time");
    }

    if (request.tracks() == null || request.tracks().length < 2 || request.tracks().length > 3) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "An event must have 2 or 3 tracks.");
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
            season);

    hackathonEventRepository.save(hackathonEvent);

    for (CreateTrackRequestDto trackReq : request.tracks()) {
      Track track = new Track(trackReq.name(), trackReq.description(), hackathonEvent);
      trackRepository.save(track);
    }

    return new HackathonEventDto(
        hackathonEvent.getId(),
        hackathonEvent.getName(),
        hackathonEvent.getDescription(),
        HackathonEventStatusDto.fromValue(hackathonEvent.getStatus().name()),
        hackathonEvent.getStartTime(),
        hackathonEvent.getEndTime(),
        hackathonEvent.getSeason().getId());
  }
}
