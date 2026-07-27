package seal.backend.services.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import seal.backend.entities.AuditLog;
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
import seal.backend.entities.audit.GradingLog;
import seal.backend.entities.notification.ScoreDeviationNotif;
import seal.backend.enums.Role;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.ScoreDeviationNotifRepository;
import seal.backend.repositories.ScoreRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.SubmissionService;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
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
  private final CriteriaRepository criteriaRepo;
  private final AuditLogRepository<AuditLog> auditLogRepo;

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

    OffsetDateTime now = OffsetDateTime.now();
    if (activeRound.getActiveTime() == null
        | now.isAfter(activeRound.getActiveTime().plus(activeRound.getActiveDuration()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot submit right now");
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Student actor = studentRepo.findByEmail(auth.getName()).get();

    Team studentTeam =
        actor.getTeams().stream()
            .filter(pred -> pred.getHackathonEvent().equals(event))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not in a team"));

    if (studentTeam.getEliminatedAtRound() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team is eliminated.");
    }

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

    OffsetDateTime now = OffsetDateTime.now();
    if (submission.getRound().getGradingStartTime() == null
        || now.isAfter(
            submission
                .getRound()
                .getGradingStartTime()
                .plus(submission.getRound().getGradingDuration()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grading is not allowed right now");
    }

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
            HttpStatus.BAD_REQUEST, "Score must be between 0 and 100.");
      }

      if (dto.value() < 5 && dto.comment() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment is required.");
      }

      // TODO: fix ts
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
                    notif.getStatus(),
                    notif.getJudgeReason(),
                    notif.getCreatedAt()))
        .toList();
  }

  private void checkScoreDeviation(Submission submission) {
    List<Score> allScores = submission.getScores();
    Map<Lecturer, List<Score>> scoresByJudge =
        allScores.stream().collect(Collectors.groupingBy(Score::getLecturer));

    if (scoresByJudge.size() < 2) {
      return;
    }

    List<ScoreDeviationNotif> existingNotifs = notifRepo.findBySubmissionId(submission.getId());

    Set<UUID> resolvedLecturerIds =
        existingNotifs.stream()
            .filter(ScoreDeviationNotif::isResolved)
            .filter(n -> n.getLecturer() != null)
            .map(n -> n.getLecturer().getId())
            .collect(Collectors.toSet());

    Map<UUID, ScoreDeviationNotif> pendingTotalNotifs =
        existingNotifs.stream()
            .filter(n -> !n.isResolved())
            .filter(n -> n.getCriteria() == null)
            .filter(n -> n.getLecturer() != null)
            .collect(Collectors.toMap(n -> n.getLecturer().getId(), n -> n, (n1, n2) -> n1));

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

    // CHECK LỆCH TỔNG ĐIỂM
    for (Map.Entry<Lecturer, Double> entry : totalScoresPerJudge.entrySet()) {
      Lecturer lecturer = entry.getKey();
      UUID lecturerId = lecturer.getId();
      double judgeScore = entry.getValue();
      double deviation = Math.abs(judgeScore - averageTotal);

      if (resolvedLecturerIds.contains(lecturerId)) {
        continue;
      }

      boolean isCurrentlyDeviated = (deviation >= 2.0);

      if (isCurrentlyDeviated) {
        if (pendingTotalNotifs.containsKey(lecturerId)) {
          ScoreDeviationNotif existingNotif = pendingTotalNotifs.get(lecturerId);
          existingNotif.setJudgeScore(judgeScore);
          existingNotif.setAverageScore(averageTotal);
          notifRepo.save(existingNotif);
        } else {
          ScoreDeviationNotif notif =
              ScoreDeviationNotif.builder()
                  .submission(submission)
                  .lecturer(lecturer)
                  .judgeScore(judgeScore)
                  .averageScore(averageTotal)
                  .createdAt(OffsetDateTime.now())
                  .status("PENDING")
                  .isResolved(false)
                  .build();
          notifRepo.save(notif);
        }
      } else {
        if (pendingTotalNotifs.containsKey(lecturerId)) {
          notifRepo.delete(pendingTotalNotifs.get(lecturerId));
        }
      }
    }

    // CHECK LỆCH ĐIỂM THÀNH PHẦN THEO TIÊU CHÍ
    Map<UUID, List<Score>> scoresByCriteria =
        allScores.stream().collect(Collectors.groupingBy(score -> score.getCriteria().getId()));

    Map<String, ScoreDeviationNotif> pendingCriteriaNotifs =
        existingNotifs.stream()
            .filter(n -> !n.isResolved())
            .filter(n -> n.getCriteria() != null && n.getLecturer() != null)
            .collect(
                Collectors.toMap(
                    n ->
                        n.getLecturer().getId().toString()
                            + "_"
                            + n.getCriteria().getId().toString(),
                    n -> n,
                    (n1, n2) -> n1));

    for (Map.Entry<UUID, List<Score>> entry : scoresByCriteria.entrySet()) {
      UUID criteriaId = entry.getKey();
      List<Score> criteriaScores = entry.getValue();

      if (criteriaScores.size() >= 2) {
        double avgCriteria =
            criteriaScores.stream().mapToDouble(Score::getValue).average().orElse(0.0);

        for (Score s : criteriaScores) {
          Lecturer lecturer = s.getLecturer();
          UUID lecturerId = lecturer.getId();
          double judgeScore = (double) s.getValue();
          double deviation = Math.abs(judgeScore - avgCriteria);

          if (resolvedLecturerIds.contains(lecturerId)) {
            continue;
          }

          String mapKey = lecturerId.toString() + "_" + criteriaId.toString();
          boolean isCurrentlyDeviated = (deviation >= 2.0);

          if (isCurrentlyDeviated) {
            if (pendingCriteriaNotifs.containsKey(mapKey)) {
              ScoreDeviationNotif existingNotif = pendingCriteriaNotifs.get(mapKey);
              existingNotif.setJudgeScore(judgeScore);
              existingNotif.setAverageScore(avgCriteria);
              notifRepo.save(existingNotif);
            } else {
              ScoreDeviationNotif notif =
                  ScoreDeviationNotif.builder()
                      .submission(submission)
                      .criteria(s.getCriteria())
                      .lecturer(lecturer)
                      .judgeScore(judgeScore)
                      .averageScore(avgCriteria)
                      .createdAt(OffsetDateTime.now())
                      .status("PENDING")
                      .isResolved(false)
                      .build();
              notifRepo.save(notif);
            }
          } else {
            if (pendingCriteriaNotifs.containsKey(mapKey)) {
              notifRepo.delete(pendingCriteriaNotifs.get(mapKey));
            }
          }
        }
      }
    }
  }

  @Override
  @Transactional
  public void acceptDeviation(UUID submissionId, UUID notifId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Lecturer actor =
        lecturerRepo
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    ScoreDeviationNotif notif =
        notifRepo
            .findById(notifId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notif not found."));

    if (!notif.getLecturer().getId().equals(actor.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This notification is not for you.");
    }
    if (notif.isResolved()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already resolved.");
    }

    Submission submission = notif.getSubmission();

    List<Score> oldScores =
        submission.getScores().stream()
            .filter(s -> s.getLecturer().getId().equals(actor.getId()))
            .toList();

    // Lưu Audit Log
    for (Score oldScore : oldScores) {
      GradingLog gradingLog =
          GradingLog.builder()
              .actionTime(OffsetDateTime.now())
              .actor(actor)
              .submission(submission)
              .action("DELETED_SCORE_BY_LECTURER")
              .details("Lecturer accepted deviation and deleted old score.")
              .build();
      auditLogRepo.save(gradingLog);
    }

    scoreRepo.deleteAll(oldScores);
    submission.getScores().removeAll(oldScores);

    notif.setResolved(true);
    notif.setStatus("ACCEPTED");
    notifRepo.save(notif);
    submissionRepo.save(submission);
  }

  @Override
  @Transactional
  public void rejectDeviation(UUID submissionId, UUID notifId, String reason) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Lecturer actor =
        lecturerRepo
            .findByEmail(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));

    ScoreDeviationNotif notif =
        notifRepo
            .findById(notifId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notif not found."));

    if (!notif.getLecturer().getId().equals(actor.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This notification is not for you.");
    }

    notif.setResolved(true);
    notif.setStatus("REJECTED");
    notif.setJudgeReason(reason);
    notifRepo.save(notif);
  }
}
