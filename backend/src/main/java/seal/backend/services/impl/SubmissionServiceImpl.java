package seal.backend.services.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import seal.backend.entities.Criteria;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Round;
import seal.backend.entities.Score;
import seal.backend.entities.Student;
import seal.backend.entities.Submission;
import seal.backend.entities.Team;
import seal.backend.entities.Track;
import seal.backend.entities.User;
import seal.backend.entities.notification.RegradeNotif;
import seal.backend.entities.notification.ScoreDeviationNotif;
import seal.backend.enums.Role;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.RegradeNotifRepository;
import seal.backend.repositories.ScoreDeviationNotifRepository;
import seal.backend.repositories.ScoreRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.SubmissionService;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
import seal.openapi.model.RequestRegradePayloadDto;
import seal.openapi.model.ScoreDeviationNotifDto;
import seal.openapi.model.SubmissionDto;
import seal.openapi.model.SubmitWorkRequestDto;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
  private final SubmissionRepository submissionRepo;
  private final HackathonEventRepository eventRepo;
  private final StudentRepository studentRepo;
  private final LecturerRepository lecturerRepo;
  private final UserRepository userRepo;
  private final TeamRepository teamRepo;
  private final ScoreRepository scoreRepo;
  private final ScoreDeviationNotifRepository notifRepo;
  private final RegradeNotifRepository regradeNotifRepo;
  private final CriteriaRepository criteriaRepo;
  private final AuditLogRepository auditLogRepo;

  private final Pattern githubPattern = Pattern.compile("^(https?://)?github\\.com");
  private final Pattern ytPattern = Pattern.compile("^(https?://)?youtube\\.com");

  @Override
  @Transactional
  public void submitWork(UUID eventId, SubmitWorkRequestDto request) {
    if (!githubPattern.matcher(request.githubLink()).find()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "GitHub link must be a valid HTTP(S) link to GitHub");
    }
    if (!ytPattern.matcher(request.youtubeLink()).find()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "YouTube link must be a valid HTTP(S) link to YouTube");
    }

    HackathonEvent event =
        eventRepo
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event does not exist."));

    Round activeRound =
        event
            .getActiveRound()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is not ongoing."));

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Student actor = studentRepo.findByEmail(auth.getName()).get();

    Team studentTeam =
        actor.getTeams().stream()
            .filter(pred -> pred.getHackathonEvent().equals(event))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in a team"));

    if (!actor.isTeamLeaderOf(studentTeam)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only team leader can submit works.");
    }

    if (!studentTeam.isTeamValid()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Team is not eligible to participate.");
    }

    Submission submission =
        new Submission(
            OffsetDateTime.now(),
            request.title(),
            request.description(),
            request.githubLink(),
            request.youtubeLink(),
            request.slideLink(),
            studentTeam,
            activeRound);

    submissionRepo.save(submission);
  }

  @Override
  @Transactional
  public SubmissionDto getSubmissionById(UUID submissionId) {
    Submission submission =
        submissionRepo
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission doesn't exist."));

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User actor = userRepo.findByEmail(auth.getName()).get();

    if (actor.getRole() == Role.STUDENT) {
      Student student = studentRepo.findById(actor.getId()).get();
      if (!student.getTeams().contains(submission.getSubmitterTeam())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't belong to this team.");
      }
    } else if (actor.getRole() == Role.LECTURER) {
      Lecturer lecturer = lecturerRepo.findByEmail(actor.getEmail()).get();
      Track track = submission.getSubmitterTeam().getTrack();

      if (track != null
          && !lecturer.getJudgedTracks().contains(track)
          && !track.getMentors().contains(lecturer)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "You are not assigned to view this team's submissions.");
      }
    }

    return submission.toDto();
  }

  @Override
  @Transactional
  public List<SubmissionDto> getSubmissionsByEventId(UUID eventId) {
    return submissionRepo.findAllBySubmitterTeamHackathonEventId(eventId).stream()
        .map(Submission::toDto)
        .toList();
  }

  @Override
  @Transactional
  public List<SubmissionDto> getAllSubmissions(UUID teamId) {
    Team targetTeam =
        teamRepo
            .findById(teamId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team does not exist."));

    if (targetTeam.getTrack() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Team has not yet been assigned to a track.");
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User actor = userRepo.findByEmail(auth.getName()).get();

    if (actor.getRole() == Role.STUDENT) {
      // Does this actor belong to the team they want to view?
      Student student = studentRepo.findById(actor.getId()).get();
      if (!student.getTeams().contains(targetTeam)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't belong to this team.");
      }

      return submissionRepo.findAllBySubmitterTeamId(teamId).stream()
          .map(Submission::toDto)
          .toList();
    } else if (actor.getRole() == Role.LECTURER) {
      // CONSTRAINTS:
      // Only mentor on the correct track or can view submissions
      // Only judge assigned to the correct track can view submissions
      Lecturer lecturer = lecturerRepo.findByEmail(actor.getEmail()).get();

      if (lecturer.getJudgedTracks().contains(targetTeam.getTrack())) {
        // actor is a judge assigned to the same track as the team, return submissions
        return submissionRepo.findAllBySubmitterTeamId(teamId).stream()
            .map(Submission::toDto)
            .toList();
      }

      if (targetTeam.getTrack().getMentors().contains(lecturer)) {
        // Actor is a mentor assigned to the same track as the team, return submissions
        return submissionRepo.findAllBySubmitterTeamId(teamId).stream()
            .map(Submission::toDto)
            .toList();
      }
    }

    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN, "You are not allowed to view teams' submissions.");
  }

  @Transactional
  @Override
  public void gradeSubmission(UUID submissionId, GradeSubmissionRequestArrayItemDto[] scores) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Lecturer actor =
        lecturerRepo
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    Submission submission =
        submissionRepo
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission doesn't exist."));

    // Constraint: can only grade submissions belonging to teams on the same track as the lecturer
    if (!actor.getJudgedTracks().contains(submission.getSubmitterTeam().getTrack())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Can only grade submissions belonging to teams on the same track as the lecturer.");
    }

    // Block overwriting
    List<Score> existingScores =
        submission.getScores().stream()
            .filter(s -> s.getLecturer().getId().equals(actor.getId()))
            .toList();

    if (!existingScores.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "You have already graded this article. Please submit a request for re-grading if you want"
              + " to correct your score.");
    }

    List<Score> newScores = new ArrayList<>();

    for (GradeSubmissionRequestArrayItemDto dto : scores) {
      if (dto.value() < 0 || dto.value() > 10) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Score must be between 0 and 10.");
      }

      if (dto.value() < 5 && dto.comment() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment is required.");
      }

      // Make sure the criteria actually is a criteria for the round
      // Make sure the criteria actually is a criteria for the round
      // Criteria criteria =
      //     submission.getRound().getCriteria().stream()
      //         .filter(pred -> pred.getId().equals(dto.criteriaId()))
      //         .findAny()
      //         .orElseThrow(
      //             () ->
      //                 new ResponseStatusException(
      //                     HttpStatus.BAD_REQUEST, "This criteria does not exist on this
      // round."));

      Criteria criteria =
          criteriaRepo
              .findById(dto.criteriaId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.BAD_REQUEST, "This criteria does not exist."));

      Score givenScore = new Score(criteria, submission, actor, dto.value());
      givenScore.setComment(dto.comment());
      submission.getScores().add(givenScore);

      newScores.add(givenScore);
    }

    scoreRepo.saveAll(newScores);
    submissionRepo.save(submission);
    checkScoreDeviation(submission);
  }

  @Override
  public void requestRegrade(UUID submissionId, RequestRegradePayloadDto payload) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Lecturer actor =
        lecturerRepo
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    Submission submission =
        submissionRepo
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission doesn't exist"));

    // Avoid spamming with multiple requests
    if (regradeNotifRepo
        .findBySubmissionIdAndLecturerIdAndIsResolvedFalse(submissionId, actor.getId())
        .isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Your request is awaiting Coordinator approval.");
    }

    RegradeNotif notif =
        RegradeNotif.builder()
            .submission(submission)
            .lecturer(actor)
            .reason(payload.reason())
            .isResolved(false)
            .createdAt(OffsetDateTime.now())
            .build();
    regradeNotifRepo.save(notif);
  }

  @Override
  @Transactional
  public void approveRegrade(UUID submissionId) {
    List<RegradeNotif> pendingNotifs =
        regradeNotifRepo.findBySubmissionIdAndIsResolvedFalse(submissionId);

    if (pendingNotifs.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "There are no requests for regrading.");
    }

    Submission submission = submissionRepo.findById(submissionId).get();

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    User actor = userRepo.findByEmail(auth.getName()).get();

    for (RegradeNotif notif : pendingNotifs) {
      notif.setResolved(true);

      List<Score> oldScores =
          submission.getScores().stream()
              .filter(s -> s.getLecturer().getId().equals(notif.getLecturer().getId()))
              .toList();

      for (Score oldScore : oldScores) {
        seal.backend.entities.audit.GradingLog gradingLog =
            seal.backend.entities.audit.GradingLog.builder()
                .actionTime(OffsetDateTime.now())
                .actor(actor)
                .submission(submission)
                .action("DELETED_SCORE")
                .details(
                    "Coordinator approved regrade. Deleted old score value: "
                        + oldScore.getValue()
                        + " graded by lecturer ID: "
                        + oldScore.getLecturer().getId())
                .build();
        auditLogRepo.save(gradingLog);
      }

      scoreRepo.deleteAll(oldScores);
      submission.getScores().removeAll(oldScores);
    }

    regradeNotifRepo.saveAll(pendingNotifs);
    submissionRepo.save(submission);

    // Change the status of alerts to processed (true)
    List<ScoreDeviationNotif> deviations = notifRepo.findBySubmissionId(submissionId);
    for (ScoreDeviationNotif d : deviations) {
      d.setResolved(true);
    }
    notifRepo.saveAll(deviations);
  }

  @Override
  public List<ScoreDeviationNotifDto> getScoreDeviations(UUID submissionId) {
    return notifRepo.findBySubmissionId(submissionId).stream()
        .map(
            notif ->
                new ScoreDeviationNotifDto(
                    notif.getId(),
                    notif.getSubmission().getId(),
                    notif.getCriteria() != null ? notif.getCriteria().getId() : null,
                    notif.getLecturer().getId(),
                    notif.getJudgeScore(),
                    notif.getAverageScore(),
                    notif.isResolved(),
                    notif.getCreatedAt()))
        .toList();
  }

  private void checkScoreDeviation(Submission submission) {
    // Remove any unprocessed alerts before rescanning to avoid data duplication.
    List<ScoreDeviationNotif> oldDeviations =
        notifRepo.findBySubmissionId(submission.getId()).stream()
            .filter(n -> !n.isResolved())
            .toList();
    if (!oldDeviations.isEmpty()) {
      notifRepo.deleteAll(oldDeviations);
    }

    List<Score> allScores = submission.getScores();
    Map<Lecturer, List<Score>> scoresByJudge =
        allScores.stream().collect(Collectors.groupingBy(Score::getLecturer));

    if (scoresByJudge.size() >= 2) {
      Map<Lecturer, Double> totalScoresPerJudge = new HashMap<>();
      for (Map.Entry<Lecturer, List<Score>> entry : scoresByJudge.entrySet()) {
        double totalScore =
            entry.getValue().stream()
                .mapToDouble(s -> s.getValue() * s.getCriteria().getWeight() / 100.0)
                .sum();
        totalScoresPerJudge.put(entry.getKey(), totalScore);
      }

      double averageTotal =
          totalScoresPerJudge.values().stream()
              .mapToDouble(Double::doubleValue)
              .average()
              .orElse(0.0);

      for (Map.Entry<Lecturer, Double> entry : totalScoresPerJudge.entrySet()) {
        double deviation = Math.abs(entry.getValue() - averageTotal);

        if (deviation >= 20.0) {
          ScoreDeviationNotif notif =
              ScoreDeviationNotif.builder()
                  .submission(submission)
                  .lecturer(entry.getKey())
                  .judgeScore(entry.getValue())
                  .averageScore(averageTotal)
                  .createdAt(OffsetDateTime.now())
                  .isResolved(false)
                  .build();
          notifRepo.save(notif);
        }
      }

      Map<UUID, List<Score>> scoresByCriteria =
          allScores.stream().collect(Collectors.groupingBy(score -> score.getCriteria().getId()));

      for (Map.Entry<UUID, List<Score>> entry : scoresByCriteria.entrySet()) {
        if (entry.getValue().size() >= 2) {
          double avgCriteria =
              entry.getValue().stream().mapToInt(Score::getValue).average().orElse(0.0);

          for (Score s : entry.getValue()) {
            double deviation = Math.abs(s.getValue() - avgCriteria);

            if (deviation >= 2.0) {
              ScoreDeviationNotif notif =
                  ScoreDeviationNotif.builder()
                      .submission(submission)
                      .criteria(s.getCriteria())
                      .lecturer(s.getLecturer())
                      .judgeScore((double) s.getValue())
                      .averageScore(avgCriteria)
                      .createdAt(OffsetDateTime.now())
                      .isResolved(false)
                      .build();
              notifRepo.save(notif);
            }
          }
        }
      }
    }
  }
}
