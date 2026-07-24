package seal.backend.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seal.backend.config.GlobalConfig;
import seal.backend.services.HackathonEventService;
import seal.backend.services.RoundService;
import seal.backend.services.SubmissionService;
import seal.backend.services.TrackService;
import seal.openapi.api.EventsApi;
import seal.openapi.model.CreateEventRequestDto;
import seal.openapi.model.CreateRoundRequestDto;
import seal.openapi.model.CreateTrackRequestDto;
import seal.openapi.model.HackathonEventDto;
import seal.openapi.model.RoundDto;
import seal.openapi.model.StudentDto;
import seal.openapi.model.SubmissionRankDto;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.SubmitWorkRequestDto;
import seal.openapi.model.TeamDto;
import seal.openapi.model.TrackDto;
import seal.openapi.model.UpdateEventRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(GlobalConfig.API_BASE)
public class HackathonEventController implements EventsApi {
  private final HackathonEventService eventService;
  private final SubmissionService submissionService;
  private final TrackService trackService;
  private final RoundService roundService;

  @Override
  @PreAuthorize("hasAnyAuthority('COORDINATOR', 'STUDENT')")
  public ResponseEntity<StudentDto[]> getInterestedParticipants(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    return ResponseEntity.ok(
        eventService.getInterestedParticipants(eventId).toArray(StudentDto[]::new));
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<HackathonEventDto> updateEvent(
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @Valid @RequestBody @NotNull UpdateEventRequestDto request) {
    return ResponseEntity.ok(eventService.updateEvent(eventId, request));
  }

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<Void> markInterested(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    eventService.markInterested(eventId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<Void> finalizeEvent(@PathVariable(name = "eventId") @NotNull UUID eventId) {
    eventService.finalizeEvent(eventId);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<HackathonEventDto> createEvent(
      @RequestBody @Valid @NotNull CreateEventRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
  }

  @Override
  public ResponseEntity<HackathonEventDto[]> getEventsInSeason(
      @PathVariable(name = "seasonId") @NotNull UUID seasonId) {
    return ResponseEntity.ok(eventService.getAllEvents(seasonId).toArray(HackathonEventDto[]::new));
  }

  @Override
  public ResponseEntity<TeamDto[]> getAllTeamsOfEvents(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    List<TeamDto> dtos = eventService.getAllTeamsOfEvent(eventId);
    return ResponseEntity.ok(dtos.toArray(TeamDto[]::new));
  }

  @Override
  public ResponseEntity<HackathonEventDto> getEvent(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    return ResponseEntity.ok(eventService.getEvent(eventId));
  }

  @Override
  @PreAuthorize("hasAuthority('STUDENT')")
  public ResponseEntity<Void> submitWork(
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull SubmitWorkRequestDto body) {
    submissionService.submitWork(eventId, body);
    return ResponseEntity.ok().build();
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<RoundDto> createRound(
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull CreateRoundRequestDto request) {
    RoundDto responseDto = roundService.createRound(eventId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @Override
  @PreAuthorize("hasAuthority('COORDINATOR')")
  public ResponseEntity<TrackDto> createTrack(
      @PathVariable(name = "eventId") @NotNull UUID eventId,
      @RequestBody @Valid @NotNull CreateTrackRequestDto request) {
    TrackDto responseDto = trackService.createTrack(request, eventId);
    return ResponseEntity.ok(responseDto);
  }

  @Override
  public ResponseEntity<TrackDto[]> getAllTracksOfEvent(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    List<TrackDto> dtos = eventService.getAllTracksOfEvent(eventId);
    return ResponseEntity.ok(dtos.toArray(TrackDto[]::new));
  }

  @Override
  public ResponseEntity<RoundDto[]> getRounds(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    return ResponseEntity.ok(roundService.getRounds(eventId).toArray(RoundDto[]::new));
  }

  @Override
  public ResponseEntity<SubmissionRankDto[]> getRanking(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    List<SubmissionRankDto> rankings = eventService.getRanking(eventId);
    return ResponseEntity.ok(rankings.toArray(SubmissionRankDto[]::new));
  }
    
  @PreAuthorize("hasAuthority('COORDINATOR') or hasRole('LECTURER')")
  public ResponseEntity<SubmissionDto[]> getSubmissionsByEvent(
      @PathVariable(name = "eventId") @NotNull UUID eventId) {
    List<SubmissionDto> dtos = submissionService.getSubmissionsByEventId(eventId);
    return ResponseEntity.ok(dtos.toArray(SubmissionDto[]::new));
  }
}
