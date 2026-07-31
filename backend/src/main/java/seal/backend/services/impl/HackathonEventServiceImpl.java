package seal.backend.services.impl;

import jakarta.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
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
    List<Submission> allSubmissions =
        submissionRepository.findAllBySubmitterTeamHackathonEventId(eventId);

    // Chỉ lấy bài nộp MỚI NHẤT đã được chấm điểm của mỗi team
    Map<Team, Submission> teamLatestSubmission = new java.util.HashMap<>();
    for (Submission s : allSubmissions) {
      if (s.calculateAvgScore() != null) {
        Team t = s.getSubmitterTeam();
        if (!teamLatestSubmission.containsKey(t)
            || s.getSubmitTime().isAfter(teamLatestSubmission.get(t).getSubmitTime())) {
          teamLatestSubmission.put(t, s);
        }
      }
    }

    // 2. Sắp xếp các bài nộp mới nhất theo điểm số giảm dần, nếu bằng điểm thì ưu tiên nộp sớm
    return teamLatestSubmission.values().stream()
        .sorted(
            java.util.Comparator.comparing(
                    Submission::calculateAvgScore,
                    java.util.Comparator.nullsLast(Double::compareTo))
                .reversed()
                .thenComparing(Submission::getSubmitTime))
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

    // make sure grading has finished
    if (activeRound.getGradingStartTime() == null
        || activeRound
            .getGradingStartTime()
            .plus(activeRound.getGradingDuration())
            .isAfter(OffsetDateTime.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The current round has not yet ended.");
    }

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

      List<Team> advancingTeams = topTeams.subList(0, Math.min(2, topTeams.size()));
      for (Team team : teams) {
        if (!advancingTeams.contains(team)) {
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

  @Transactional
  @Override
  public void start(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event does not exist."));

    if (event.hasStarted()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event has already started.");
    }

    if (event.getRounds().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Event does not have any rounds configured.");
    }

    Round firstRound = event.getRounds().getFirst();
    firstRound.setActiveTime(OffsetDateTime.now());
    roundRepo.save(firstRound);
  }

  @Transactional
  @Override
  public void startGrading(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event does not exist."));

    Round roundToGrade =
        event
            .getRounds()
            .stream()
            .filter(r -> r.getActiveTime() != null && r.getGradingStartTime() == null)
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No round currently available to start grading."));

    roundToGrade.setGradingStartTime(OffsetDateTime.now());
    roundRepo.save(roundToGrade);
  }

  @Override
  public byte[] exportTeamCertificate(UUID eventId, UUID teamId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    Team team =
        teamRepo
            .findById(teamId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

    if (!team.getHackathonEvent().getId().equals(eventId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Team does not belong to this event");
    }

    // đọc file ảnh
    try {
      ClassPathResource templateResource =
          new ClassPathResource("templates/certificate_template.png");
      BufferedImage image = ImageIO.read(templateResource.getInputStream());
      Graphics2D g2d = image.createGraphics();

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      // Setup Font chữ (Tùy chỉnh tên font, kiểu in đậm và size cho hợp với template)
      ClassPathResource fontResource = new ClassPathResource("fonts/CatchyMager.ttf");
      Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontResource.getInputStream());

      Font finalFont = customFont.deriveFont(Font.PLAIN, 120);
      g2d.setFont(finalFont);
      g2d.setColor(Color.BLACK);

      String teamName = team.getName().toUpperCase();

      FontMetrics fontMetrics = g2d.getFontMetrics(finalFont);
      int textWidth = fontMetrics.stringWidth(teamName);

      int x = (image.getWidth() - textWidth) / 2; // Căn giữa theo chiều ngang
      int y = (image.getHeight() / 2) - 25; // Căn giữa theo chiều dọc

      g2d.drawString(teamName, x, y);
      g2d.dispose();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();

    } catch (Exception e) {
      e.printStackTrace();
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate certificate");
    }
  }

  @Override
  @Transactional
  public String exportEventRankingCsv(UUID eventId) {
    HackathonEvent event =
        hackathonEventRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    List<Submission> allSubmissions =
        submissionRepository.findAllBySubmitterTeamHackathonEventId(eventId);

    // Chỉ lấy submission mới nhất của mỗi team và đã được chấm điểm
    Map<Team, Submission> teamLatestSubmission = new java.util.HashMap<>();
    for (Submission s : allSubmissions) {
      if (s.calculateAvgScore() != null) {
        Team t = s.getSubmitterTeam();
        if (!teamLatestSubmission.containsKey(t)
            || s.getSubmitTime().isAfter(teamLatestSubmission.get(t).getSubmitTime())) {
          teamLatestSubmission.put(t, s);
        }
      }
    }

    List<Submission> rankedSubmissions =
        teamLatestSubmission.values().stream()
            .sorted(
                java.util.Comparator.comparing(
                        Submission::calculateAvgScore,
                        java.util.Comparator.nullsLast(Double::compareTo))
                    .reversed()
                    .thenComparing(Submission::getSubmitTime))
            .toList();

    Set<String> criteriaNames = new java.util.LinkedHashSet<>();
    for (Submission s : rankedSubmissions) {
      for (seal.backend.entities.Score score : s.getScores()) {
        criteriaNames.add(score.getCriteria().getName());
      }
    }

    StringBuilder csv = new StringBuilder();
    csv.append('\ufeff');

    csv.append("\"BẢNG ĐIỂM XẾP HẠNG - ")
        .append(event.getName().toUpperCase().replace("\"", "\"\""))
        .append("\"\n\n");

    csv.append("Thứ hạng,Tên Đội");
    for (String cName : criteriaNames) {
      csv.append(",\"").append(cName.replace("\"", "\"\"")).append("\"");
    }
    csv.append(",Tổng điểm\n");

    int rank = 1;
    for (Submission s : rankedSubmissions) {
      csv.append(rank++).append(",");
      csv.append("\"").append(s.getSubmitterTeam().getName().replace("\"", "\"\"")).append("\",");

      Map<String, Double> totalScorePerCriteria = new java.util.HashMap<>();
      Map<String, Integer> judgeCountPerCriteria = new java.util.HashMap<>();

      for (seal.backend.entities.Score score : s.getScores()) {
        String cName = score.getCriteria().getName();
        double weightedScore = (score.getValue() * score.getCriteria().getWeight()) / 100.0;

        totalScorePerCriteria.put(
            cName, totalScorePerCriteria.getOrDefault(cName, 0.0) + weightedScore);
        judgeCountPerCriteria.put(cName, judgeCountPerCriteria.getOrDefault(cName, 0) + 1);
      }

      for (String cName : criteriaNames) {
        if (totalScorePerCriteria.containsKey(cName)) {
          double avgCriteriaScore =
              totalScorePerCriteria.get(cName) / judgeCountPerCriteria.get(cName);
          csv.append(String.format(java.util.Locale.US, "%.2f", avgCriteriaScore)).append(",");
        } else {
          csv.append("0.00,");
        }
      }

      csv.append(String.format(java.util.Locale.US, "%.2f", s.calculateAvgScore())).append("\n");
    }

    return csv.toString();
  }
}
