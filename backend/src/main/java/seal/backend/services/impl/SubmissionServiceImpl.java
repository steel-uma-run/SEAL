package seal.backend.services.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
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
import seal.backend.entities.User;
import seal.backend.enums.Role;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.ScoreRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.UserRepository;
import seal.backend.services.SubmissionService;
import seal.openapi.model.GradeSubmissionRequestArrayItemDto;
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
  private final CriteriaRepository criteriaRepo;
  private final ScoreRepository scoreRepo;

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
      // does this actor belong to the team they want to view?
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
        // actor is a mentor assigned to the same track as the team, return submissions
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
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission doesn't exist"));

    // Constraint: can only grade submissions belonging to teams on the same track as the lecturer
    if (!actor.getJudgedTracks().contains(submission.getSubmitterTeam().getTrack())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    for (GradeSubmissionRequestArrayItemDto dto : scores) {
      // Constraint: score must be between 0-100
      if (dto.value() < 0 | dto.value() > 100) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Score must be between 0 and 100");
      }

      // Constraint: if score is below average, require a comment
      if (dto.value() < 50 && dto.comment() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A comment is required");
      }

      Criteria criteria =
          criteriaRepo
              .findById(dto.criteriaId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(HttpStatus.NOT_FOUND, "Criteria doesn't exist"));

      Score givenScore = new Score(criteria, submission, actor, dto.value());
      givenScore.setComment(dto.comment());
      submission.getScores().add(givenScore);

      scoreRepo.save(givenScore);
      submissionRepo.save(submission);
    }
  }
}
