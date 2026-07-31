package seal.backend.config;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import seal.backend.entities.AuditLog;
import seal.backend.entities.Coordinator;
import seal.backend.entities.Criteria;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Round;
import seal.backend.entities.Score;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Submission;
import seal.backend.entities.Team;
import seal.backend.entities.TemplatedCriteria;
import seal.backend.entities.Track;
import seal.backend.entities.audit.GradingLog;
import seal.backend.enums.EventStatus;
import seal.backend.enums.Role;
import seal.backend.enums.Semester;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.AuditLogRepository;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.repositories.ScoreRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
import seal.backend.repositories.SubmissionRepository;
import seal.backend.repositories.TeamRepository;
import seal.backend.repositories.TemplatedCriteriaRepository;
import seal.backend.repositories.TrackRepository;
import seal.backend.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

  private final SeasonRepository seasonRepo;
  private final HackathonEventRepository eventRepo;
  private final TrackRepository trackRepo;
  private final RoundRepository roundRepo;
  private final CriteriaTemplateRepository templateRepo;
  private final CriteriaRepository criteriaRepo;
  private final TemplatedCriteriaRepository templatedCriteriaRepo;
  private final UserRepository userRepo;
  private final LecturerRepository lecturerRepo;
  private final StudentRepository studentRepo;
  private final TeamRepository teamRepo;
  private final SubmissionRepository submissionRepo;
  private final ScoreRepository scoreRepo;
  private final AuditLogRepository<AuditLog> auditLogRepo;
  private final PasswordEncoder passwordEncoder;

  private final Random random =
      new Random(2026); // Fix seed để data không bị đổi liên tục mỗi lần chạy

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (seasonRepo.count() > 0) {
      log.info("DATABASE DA CO DU LIEU. BO QUA SEEDER.");
      return;
    }

    log.info("Bat dau khoi tao du lieu...");
    String defaultPwd = passwordEncoder.encode("123456");

    // ==========================================
    // 1. TẠO SEASONS & EVENTS
    // ==========================================
    Season springSeason = seasonRepo.save(new Season(Semester.SPRING, 2026));
    Season summerSeason = seasonRepo.save(new Season(Semester.SUMMER, 2026));

    String prizeStructure =
        "Tổng giá trị: 16.500.000 đồng\n"
            + "• Giải Nhất: 7.000.000 đồng và Giấy chứng nhận.\n"
            + "• Giải Nhì: 5.000.000 đồng và Giấy chứng nhận.\n"
            + "• Giải Ba: 3.000.000 đồng và Giấy chứng nhận.\n"
            + "• Giải Khuyến khích: 1.500.000 đồng và Giấy chứng nhận.";

    //    // --- KÌ SPRING 2026 ---
    //    // Đăng ký: 1/3 - 20/3
    //    OffsetDateTime regStartSpring =
    //        OffsetDateTime.of(2026, 3, 1, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    //    OffsetDateTime regEndSpring =
    //        OffsetDateTime.of(2026, 3, 20, 18, 0, 0, 0, ZoneOffset.ofHours(7));
    //
    //    HackathonEvent eventSpring =
    //        new HackathonEvent(
    //            "SEAL Hackathon Spring 2026",
    //            "Mastering Domain-Specific AI RAG Systems",
    //            Duration.ofDays(7),
    //            EventStatus.FINALIZED,
    //            springSeason,
    //            prizeStructure);
    //    eventSpring.setTeamsLimit(30);
    //    eventRepo.save(eventSpring);
    //
    //    // --- KÌ SUMMER 2026 ---
    //    // Đăng ký: 20/7 - 13/8
    //    OffsetDateTime regStartSummer =
    //        OffsetDateTime.of(2026, 7, 20, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    //    OffsetDateTime regEndSummer =
    //        OffsetDateTime.of(2026, 8, 13, 18, 0, 0, 0, ZoneOffset.ofHours(7));
    //
    //    HackathonEvent eventSummer =
    //        new HackathonEvent(
    //            "SEAL Hackathon Summer 2026",
    //            "Empowering Enterprise with AI Agents",
    //            Duration.ofDays(7),
    //            EventStatus.FINALIZED,
    //            summerSeason,
    //            prizeStructure);
    //    eventSummer.setTeamsLimit(30);
    //    eventRepo.save(eventSummer);

    // --- KÌ SPRING 2026 (FINALIZED) ---
    OffsetDateTime regStartSpring =
        OffsetDateTime.of(2026, 3, 1, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    HackathonEvent eventSpring =
        new HackathonEvent(
            "SEAL Hackathon Spring 2026",
            "Mastering RAG",
            Duration.ofDays(7),
            EventStatus.FINALIZED,
            springSeason,
            prizeStructure);
    eventSpring.setTeamsLimit(30);
    eventSpring.setRegistrationStartTime(regStartSpring);
    eventRepo.save(eventSpring);

    // --- KÌ SUMMER 2026 (FINALIZED) ---
    OffsetDateTime regStartSummer =
        OffsetDateTime.of(2026, 7, 20, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    HackathonEvent eventSummer =
        new HackathonEvent(
            "SEAL Hackathon Summer 2026",
            "AI Agents",
            Duration.ofDays(7),
            EventStatus.FINALIZED,
            summerSeason,
            prizeStructure);
    eventSummer.setTeamsLimit(30);
    eventSummer.setRegistrationStartTime(regStartSummer);
    eventRepo.save(eventSummer);

    // ==========================================
    // 2. TẠO TRACKS & ROUNDS
    // ==========================================
    Track trSpringA =
        trackRepo.save(
            new Track("Requirement & Architecture", "Kiến trúc hệ thống RAG", eventSpring));
    Track trSpringB =
        trackRepo.save(
            new Track("Testing, execution and reporting app", "Kiểm thử và báo cáo", eventSpring));
    Track trSpringC =
        trackRepo.save(new Track("Execution and reporting app", "Thực thi AI Agent", eventSpring));

    Track trSumA = trackRepo.save(new Track("AI Infrastructure", "Hạ tầng", eventSummer));
    Track trSumB = trackRepo.save(new Track("Data Pipeline & Security", "Dữ liệu", eventSummer));
    Track trSumC = trackRepo.save(new Track("Smart UI/UX & Edge AI", "Giao diện", eventSummer));

    Track[] springTracks = {trSpringA, trSpringB, trSpringC};
    Track[] summerTracks = {trSumA, trSumB, trSumC};

    // --- ROUNDS SPRING (SET CỨNG THỜI GIAN QUÁ KHỨ ĐỂ CHẤM ĐIỂM) ---
    OffsetDateTime spr1Active = OffsetDateTime.of(2026, 3, 22, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    OffsetDateTime spr1Grading = OffsetDateTime.of(2026, 3, 22, 14, 0, 0, 0, ZoneOffset.ofHours(7));

    Round rdSpring1 =
        new Round("Round 1", "Vòng loại", Duration.ofHours(10), Duration.ofHours(48), eventSpring);
    rdSpring1.setActiveTime(spr1Active);
    rdSpring1.setGradingStartTime(spr1Grading);
    rdSpring1 = roundRepo.save(rdSpring1);

    Round rdSpring2 =
        new Round("Round 2", "Chung kết", Duration.ofHours(10), Duration.ofHours(48), eventSpring);
    rdSpring2 = roundRepo.save(rdSpring2); // R2 chưa active

    // --- ROUNDS SUMMER (ĐỂ NULL THỜI GIAN ĐỂ TEST NÚT BẤM) ---
    //    Round rdSum1 =
    //        roundRepo.save(
    //            new Round(
    //                "Round 1", "Vòng loại", Duration.ofHours(10), Duration.ofHours(48),
    // eventSummer));
    //    Round rdSum2 =
    //        roundRepo.save(
    //            new Round(
    //                "Round 2", "Chung kết", Duration.ofHours(10), Duration.ofHours(48),
    // eventSummer));

    // --- ROUNDS SUMMER DEMO ---
    // Nộp bài (Active): 5 phút | Chấm điểm (Grading): 5 phút
    Round rdSum1 =
        roundRepo.save(
            new Round(
                "Round 1", "Vòng loại", Duration.ofMinutes(5), Duration.ofMinutes(5), eventSummer));
    Round rdSum2 =
        roundRepo.save(
            new Round(
                "Round 2", "Chung kết", Duration.ofMinutes(5), Duration.ofMinutes(5), eventSummer));

    // ==========================================
    // 3. TẠO TIÊU CHÍ (CRITERIA)
    // ==========================================
    CriteriaTemplate tempV1 =
        templateRepo.save(new CriteriaTemplate("Tiêu chí Vòng 1", "Tiêu chí Vòng 1"));
    CriteriaTemplate tempV2 =
        templateRepo.save(new CriteriaTemplate("Tiêu chí Vòng 2", "Tiêu chí Vòng 2"));

    List<TemplatedCriteria> v1Criterias =
        templatedCriteriaRepo.saveAll(
            Arrays.asList(
                new TemplatedCriteria("Tính đúng đắn", "Mô tả tính đúng đắn", 30, tempV1),
                new TemplatedCriteria("Ứng dụng AI", "Mô tả ứng dụng AI", 25, tempV1),
                new TemplatedCriteria("Kiến trúc phần mềm", "Mô tả thiết kế", 15, tempV1),
                new TemplatedCriteria("Thuyết trình & Demo", "Thuyết trình", 20, tempV1),
                new TemplatedCriteria("Teamwork", "Teamwork", 10, tempV1)));

    List<TemplatedCriteria> v2Criterias =
        templatedCriteriaRepo.saveAll(
            Arrays.asList(
                new TemplatedCriteria(
                    "Độ hoàn thiện & Chất lượng sản phẩm", "Chất lượng", 25, tempV2),
                new TemplatedCriteria("Sáng tạo & Khả năng đổi mới", "Sáng tạo", 25, tempV2),
                new TemplatedCriteria(
                    "Tính ứng dụng & Khả năng triển khai", "Triển khai", 20, tempV2),
                new TemplatedCriteria("Trình bày & Demo sản phẩm V2", "Demo V2", 20, tempV2),
                new TemplatedCriteria(
                    "Làm việc nhóm & Trả lời phản biện", "Phản biện", 10, tempV2)));

    for (TemplatedCriteria tc : v1Criterias) {
      rdSpring1
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSpring1));
      rdSum1
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSum1));
    }
    criteriaRepo.saveAll(rdSpring1.getCriteria());
    criteriaRepo.saveAll(rdSum1.getCriteria());

    for (TemplatedCriteria tc : v2Criterias) {
      rdSpring2
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSpring2));
      rdSum2
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSum2));
    }
    criteriaRepo.saveAll(rdSpring2.getCriteria());
    criteriaRepo.saveAll(rdSum2.getCriteria());

    roundRepo.saveAll(Arrays.asList(rdSpring1, rdSpring2, rdSum1, rdSum2));

    // ==========================================
    // 4. GIÁM KHẢO (LECTURERS) & MAPPING
    // ==========================================
    userRepo.save(
        Coordinator.builder()
            .fullName("Master Coordinator")
            .role(Role.COORDINATOR)
            .email("admin@seal.edu.vn")
            .passwordHash(defaultPwd)
            .build());

    Lecturer lNam = createLecturer("Lê Hoàng Nam", "namlh@seal.edu.vn", defaultPwd);
    Lecturer lNgoc = createLecturer("Phạm Bích Ngọc", "ngocpb@seal.edu.vn", defaultPwd);
    Lecturer lAnh = createLecturer("Vũ Đức Anh", "anhvd@seal.edu.vn", defaultPwd);
    Lecturer lMai = createLecturer("Trần Thanh Mai", "maitt@seal.edu.vn", defaultPwd);
    Lecturer lSon = createLecturer("Đặng Thái Sơn", "sondt@seal.edu.vn", defaultPwd);
    Lecturer lHuong = createLecturer("Nguyễn Quỳnh Hương", "huongnq@seal.edu.vn", defaultPwd);
    Lecturer lBao = createLecturer("Bùi Quốc Bảo", "baobq@seal.edu.vn", defaultPwd);
    Lecturer lDung = createLecturer("Hồ Trí Dũng", "dunght@seal.edu.vn", defaultPwd);
    Lecturer lTrang = createLecturer("Đinh Thu Trang", "trangdt@seal.edu.vn", defaultPwd);

    // Gán 3 Judges và 2 Mentors cho mỗi Track
    assignMentorAndJudges(trSpringA, Arrays.asList(lNam, lNgoc, lAnh), Arrays.asList(lMai, lSon));
    assignMentorAndJudges(trSpringB, Arrays.asList(lMai, lSon, lHuong), Arrays.asList(lBao, lDung));
    assignMentorAndJudges(
        trSpringC, Arrays.asList(lBao, lDung, lTrang), Arrays.asList(lNam, lNgoc));

    assignMentorAndJudges(trSumA, Arrays.asList(lNam, lMai, lBao), Arrays.asList(lNgoc, lSon));
    assignMentorAndJudges(trSumB, Arrays.asList(lNgoc, lSon, lDung), Arrays.asList(lAnh, lHuong));
    assignMentorAndJudges(trSumC, Arrays.asList(lAnh, lHuong, lTrang), Arrays.asList(lNam, lDung));

    // ==========================================
    // 5. KHỞI TẠO CÁC TEAM CỐ ĐỊNH (CHO VIỆC DEMO CHÍNH)
    // ==========================================
    log.info("Khoi tao Teams co dinh...");

    // --- TEAM CỐ ĐỊNH 1: SLOTHUB ---
    Student sXuan =
        createStudent("Trương Hoàng Mỹ Xuân", "xuanthm@fpt.edu.vn", "SE203450", defaultPwd);
    Student sTrung =
        createStudent("Nguyễn Thành Trung", "trungnt@fpt.edu.vn", "SE203654", defaultPwd);
    Student sTriet =
        createStudent("Nguyễn Thế Triết", "trietnt@fpt.edu.vn", "SE203403", defaultPwd);
    Student sDien = createStudent("Nguyễn Khoa Điền", "diennk@fpt.edu.vn", "SE203419", defaultPwd);
    Student sTrung2 =
        createStudent("Nguyễn Thành Trung", "trungnt2@fpt.edu.vn", "SE203435", defaultPwd);

    createTeam(
        "Slothub",
        "Hệ thống Agentic RAG tối ưu hóa truy xuất dữ liệu bệnh án điện tử",
        eventSummer,
        trSumA,
        sXuan,
        sTrung,
        sTriet,
        sDien,
        sTrung2);

    // --- TEAM CỐ ĐỊNH 2: 404NOTFOUND ---
    Student sGiaHieu = createStudent("Đinh Gia Hiếu", "hieudg@fpt.edu.vn", "SE180301", defaultPwd);
    Student sHieu = createStudent("Phạm Trung Hiếu", "hieupt@fpt.edu.vn", "SE180201", defaultPwd);
    Student sKiet = createStudent("Lê Tuấn Kiệt", "kietlt@fpt.edu.vn", "SE180102", defaultPwd);
    createTeam(
        "404NotFound",
        "Hệ thống Crawler thu thập dữ liệu văn bản",
        eventSpring,
        trSpringB,
        sGiaHieu,
        sHieu,
        sKiet);

    // ==========================================
    // 6. KHỞI TẠO 50 TEAMS TỰ ĐỘNG BẰNG DYNAMIC GENERATOR (MỖI SỰ KIỆN 25 TEAMS)
    // ==========================================
    log.info("Bat dau tao 50 Team tu 3-5 thanh vien...");

    String[] profTeamNames = {
      "AI Nexus",
      "NeuralNet",
      "CloudSurfers",
      "DataCrafters",
      "CodePhantoms",
      "LogicBombs",
      "SynthWave",
      "PromptEngineers",
      "RAG Masters",
      "NLP Geeks",
      "Visionary Tech",
      "MLOps Hub",
      "Agentic Flow",
      "ByteMe",
      "Quantum Leap",
      "Tech Titans",
      "CyberCore",
      "Data Ninjas",
      "Edge Computing",
      "DevSecOps Pro",
      "Epoch 0",
      "BitMindz",
      "Red Team Gang",
      "WhaleDone",
      "Aqua team",
      "Passion Ducks",
      "Pioneers",
      "Innovators",
      "TechSavvy",
      "CodeCrafters",
      "ByteBuilders",
      "DataDrivers",
      "AI Mavericks",
      "Cloud Native",
      "Future Forge",
      "Syntax Squad",
      "Binary Bosses",
      "Logic Legends",
      "Code Command",
      "Tech Tribe",
      "Cyber Squad",
      "Data Dynasty",
      "AI Architects",
      "Cloud Collective",
      "Code Cartel",
      "Byte Brigade",
      "Tech Syndicate",
      "Data Domain",
      "AI Alliance",
      "Deep Learning"
    };

    String[] profProjects = {
      "Phát triển Agentic RAG cho HR", "Tối ưu hóa Pipeline ETL", "Bảo mật Data Leakage LLM",
      "Xây dựng VectorDB tra cứu luật", "UI/UX cho hệ thống quản trị AI",
          "Tự động hóa đánh giá Prompt",
      "Kiểm định lỗ hổng Prompt Injection", "Mạng lưới truy xuất nội bộ",
          "Thiết kế Dashboard Real-time",
      "Nhận diện thực thể (NER) tiếng Việt"
    };

    int globalStudentCounter = 1000;

    for (int i = 0; i < 50; i++) {
      // Chia đều 25 team cho Spring, 25 team cho Summer
      HackathonEvent currentEvent = (i < 25) ? eventSpring : eventSummer;
      Track currentTrack = (i < 25) ? springTracks[i % 3] : summerTracks[i % 3];
      String teamName = profTeamNames[i];
      String projectDesc = profProjects[i % profProjects.length];

      // 1 Team có từ 3 đến 5 người (1 Leader + 2->4 Members)
      Student leader = generateRealisticStudent(globalStudentCounter++, defaultPwd);
      int numMembers = random.nextInt(3) + 2; // Sinh số ngẫu nhiên 2, 3 hoặc 4
      Student[] members = new Student[numMembers];
      for (int j = 0; j < numMembers; j++) {
        members[j] = generateRealisticStudent(globalStudentCounter++, defaultPwd);
      }

      createTeam(teamName, projectDesc, currentEvent, currentTrack, leader, members);
    }

    // ==========================================
    // 7. KHỞI TẠO 50 SINH VIÊN TỰ DO
    // ==========================================
    log.info("Khoi tao sinh vien tu do (chua co team)...");
    for (int i = 0; i < 50; i++) {
      generateRealisticStudent(globalStudentCounter++, defaultPwd);
    }

    // ==========================================
    // 8. TẠO SUBMISSION VÀ SCORE (SPRING: TẤT CẢ | SUMMER: 9 BÀI - MỖI TRACK 3 BÀI)
    // ==========================================
    log.info("Bat dau tao du lieu Submission cho Spring (Tat ca) va Summer (Moi track 3 bai)...");

    List<Team> allTeams = teamRepo.findAll();

    // Khởi tạo biến đếm bài nộp cho từng Track của kỳ Summer
    int sumTrackACount = 0;
    int sumTrackBCount = 0;
    int sumTrackCCount = 0;

    for (Team team : allTeams) {
      boolean isSpringEvent = team.getHackathonEvent().getId().equals(eventSpring.getId());
      Track teamTrack = team.getTrack();

      // Lọc số lượng bài nộp cho kỳ Summer (Đảm bảo đúng 9 bài, mỗi Track 3 bài)
      if (!isSpringEvent) {
        boolean shouldSubmit = false;
        if (team.getName().equals("Slothub")) {
          shouldSubmit = true;
          sumTrackACount++; // Slothub mặc định nằm ở Track A
        } else if (teamTrack != null) {
          if (teamTrack.getId().equals(trSumA.getId()) && sumTrackACount < 3) {
            shouldSubmit = true;
            sumTrackACount++;
          } else if (teamTrack.getId().equals(trSumB.getId()) && sumTrackBCount < 3) {
            shouldSubmit = true;
            sumTrackBCount++;
          } else if (teamTrack.getId().equals(trSumC.getId()) && sumTrackCCount < 3) {
            shouldSubmit = true;
            sumTrackCCount++;
          }
        }

        // Nếu không thuộc danh sách 9 team được chọn thì bỏ qua
        if (!shouldSubmit) {
          continue;
        }
      }

      Round targetRound = isSpringEvent ? rdSpring1 : rdSum1;

      if (targetRound.getActiveTime() == null) {
        targetRound.setActiveTime(OffsetDateTime.now());
      }

      // Rút ngắn Submit Time xuống random trong vòng 60 giây để vừa vặn với 5 phút của Demo
      OffsetDateTime roundTime = targetRound.getActiveTime();
      OffsetDateTime submitTime = roundTime.plusSeconds(random.nextInt(60));

      String slug = team.getName().toLowerCase().replaceAll("[^a-z0-9]", "-");
      String submissionTitle = "Giải pháp " + team.getName() + ": " + team.getDescription();
      String submissionDesc =
          "Dự án hoàn thiện. Tài liệu chi tiết bao gồm mã nguồn, video demo sản phẩm và slide trình"
              + " bày kiến trúc hệ thống.";

      String githubLink = "https://github.com/seal-hackathon-2026/" + slug;
      String ytLink =
          "https://youtube.com/watch?v=" + java.util.UUID.randomUUID().toString().substring(0, 8);
      String slideLink =
          "https://docs.google.com/presentation/d/"
              + java.util.UUID.randomUUID().toString().substring(0, 15);

      Submission submission =
          new Submission(
              submitTime,
              submissionTitle,
              submissionDesc,
              githubLink,
              ytLink,
              slideLink,
              team,
              targetRound);
      Submission savedSubmission = submissionRepo.save(submission);

      // --- CHẤM ĐIỂM KỲ SPRING ---
      if (isSpringEvent) {
        if (teamTrack != null && !teamTrack.getJudges().isEmpty()) {
          int judgeCount = 0;
          for (Lecturer judge : teamTrack.getJudges()) {
            if (judgeCount >= 3) break;

            float baseScore = 6.0f + random.nextInt(3);
            for (Criteria criteria : targetRound.getCriteria()) {
              float judgeScore = baseScore + (random.nextBoolean() ? 0.5f : 0.0f);
              Score score = new Score(criteria, savedSubmission, judge, judgeScore);
              score.setComment("Đánh giá " + criteria.getName() + " đạt yêu cầu chuyên môn.");
              scoreRepo.save(score);
            }

            // LƯU AUDIT LOG CHO HÀNH ĐỘNG CHẤM ĐIỂM CỦA GIÁM KHẢO
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge)
                    .submission(savedSubmission)
                    .action("GRADED_SUBMISSION")
                    .details("Giám khảo đã chấm điểm bài nộp của nhóm " + team.getName())
                    .build());

            judgeCount++;
          }
        }
      }
      // --- CHẤM ĐIỂM KỲ SUMMER ---
      else {
        // 1. Xử lý riêng cho team Slothub (Chỉ 2 giám khảo chấm, chừa slot cho Demo)
        if (team.getName().equals("Slothub")) {
          if (teamTrack != null && teamTrack.getJudges().size() >= 2) {

            // Giám khảo 1
            Lecturer judge1 = teamTrack.getJudges().get(0);
            for (Criteria criteria : targetRound.getCriteria()) {
              Score score1 = new Score(criteria, savedSubmission, judge1, 8.0f);
              score1.setComment("Giải pháp rất tốt, đáp ứng đúng yêu cầu của hệ thống RAG.");
              scoreRepo.save(score1);
            }
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge1)
                    .submission(savedSubmission)
                    .action("GRADED_SUBMISSION")
                    .details("Giám khảo đã chấm điểm bài nộp của nhóm Slothub")
                    .build());

            // Giám khảo 2
            Lecturer judge2 = teamTrack.getJudges().get(1);
            for (Criteria criteria : targetRound.getCriteria()) {
              Score score2 = new Score(criteria, savedSubmission, judge2, 8.5f);
              score2.setComment("Kiến trúc Agentic RAG khá ổn định và có tính ứng dụng cao.");
              scoreRepo.save(score2);
            }
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge2)
                    .submission(savedSubmission)
                    .action("GRADED_SUBMISSION")
                    .details("Giám khảo đã chấm điểm bài nộp của nhóm Slothub.")
                    .build());
          }
        }
        // 2. 8 team còn lại
        else {
          if (teamTrack != null && !teamTrack.getJudges().isEmpty()) {
            int judgeCount = 0;
            for (Lecturer judge : teamTrack.getJudges()) {
              if (judgeCount >= 3) break;

              float baseScore = 6.5f + (random.nextInt(4) * 0.5f);
              for (Criteria criteria : targetRound.getCriteria()) {
                float judgeScore = baseScore + (random.nextInt(3) * 0.5f);
                if (judgeScore > 10.0f) judgeScore = 10.0f;

                Score score = new Score(criteria, savedSubmission, judge, judgeScore);
                score.setComment("Phần " + criteria.getName() + " triển khai tốt, sát thực tế.");
                scoreRepo.save(score);
              }

              // LƯU AUDIT LOG
              auditLogRepo.save(
                  GradingLog.builder()
                      .actionTime(OffsetDateTime.now())
                      .actor(judge)
                      .submission(savedSubmission)
                      .action("GRADED_SUBMISSION")
                      .details("Giám khảo đã chấm điểm bài nộp của nhóm " + team.getName())
                      .build());

              judgeCount++;
            }
          }
        }
      }
    }

    log.info("Hoan tat nap du lieu thanh cong!");
  }

  // ==========================================
  // HELPER METHODS
  // ==========================================
  private Lecturer createLecturer(String fullName, String email, String password) {
    return lecturerRepo.save(
        Lecturer.builder()
            .fullName(fullName)
            .role(Role.LECTURER)
            .email(email)
            .passwordHash(password)
            .build());
  }

  private void assignMentorAndJudges(Track track, List<Lecturer> judges, List<Lecturer> mentors) {
    if (judges != null) track.getJudges().addAll(judges);
    if (mentors != null) track.getMentors().addAll(mentors);
    trackRepo.save(track);
  }

  private Student generateRealisticStudent(int seedId, String password) {
    String[] hoList = {
      "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ"
    };
    String[] demList = {
      "Văn", "Thị", "Minh", "Ngọc", "Hữu", "Đức", "Thanh", "Thu", "Hải", "Gia", "Tuấn", "Hoàng"
    };
    String[] tenList = {
      "Khang", "Kiệt", "Anh", "Khoa", "Phát", "Thành", "Đạt", "Hùng", "Dũng", "Tuấn", "Huy",
      "Phương", "Thảo", "Vy", "Linh"
    };
    String[] tenKhongDauList = {
      "khang", "kiet", "anh", "khoa", "phat", "thanh", "dat", "hung", "dung", "tuan", "huy",
      "phuong", "thao", "vy", "linh"
    };

    int h = random.nextInt(hoList.length);
    int d = random.nextInt(demList.length);
    int t = random.nextInt(tenList.length);

    String fullName = hoList[h] + " " + demList[d] + " " + tenList[t];
    String emailPrefix =
        tenKhongDauList[t]
            + hoList[h].substring(0, 1).toLowerCase()
            + demList[d].substring(0, 1).toLowerCase();

    boolean isExternal = random.nextInt(100) < 20;

    if (isExternal) {
      String[] externalSchools = {"ĐH Bách Khoa", "ĐH Khoa học Tự nhiên", "ĐH CNTT"};
      String[] externalDomains = {"@hcmut.edu.vn", "@hcmus.edu.vn", "@uit.edu.vn"};

      int schoolIndex = random.nextInt(externalSchools.length);
      String schoolName = externalSchools[schoolIndex];
      String email = emailPrefix + seedId + externalDomains[schoolIndex];

      // MSSV trường ngoài thường là dải số liền (VD: 2110123)
      String mssv = "21" + (10000 + seedId);

      return createStudent(fullName, email, mssv, schoolName, StudentType.EXTERNAL, password);
    } else {
      // Sinh viên FPT
      String email = emailPrefix + seedId + "@fpt.edu.vn";
      String mssv = "SE" + (180000 + seedId);
      return createStudent(fullName, email, mssv, "Đại học FPT", StudentType.FPT, password);
    }
  }

  private Student createStudent(
      String fullName,
      String email,
      String mssv,
      String schoolName,
      StudentType type,
      String password) {
    return studentRepo.save(
        Student.builder()
            .fullName(fullName)
            .role(Role.STUDENT)
            .email(email)
            .passwordHash(password)
            .studentType(type)
            .studentStatus(StudentStatus.ACTIVE)
            .studentId(mssv)
            .schoolName(schoolName)
            .build());
  }

  private Student createStudent(String fullName, String email, String mssv, String password) {
    return createStudent(fullName, email, mssv, "Đại học FPT", StudentType.FPT, password);
  }

  private Team createTeam(
      String name,
      String description,
      HackathonEvent event,
      Track track,
      Student leader,
      Student... members) {
    if (!leader.getEvents().contains(event)) {
      leader.getEvents().add(event);
      studentRepo.save(leader);
    }
    Team team = new Team();
    team.setName(name);
    team.setDescription(description);
    team.setTeamStatus(TeamStatus.APPROVED);
    team.setHackathonEvent(event);
    team.setLeader(leader);
    team.setTrack(track);
    team.getMembers().add(leader);

    for (Student member : members) {
      if (!member.getEvents().contains(event)) {
        member.getEvents().add(event);
        studentRepo.save(member);
      }
      team.getMembers().add(member);
    }
    Team savedTeam = teamRepo.save(team);
    leader.getTeams().add(savedTeam);
    studentRepo.save(leader);
    for (Student member : members) {
      member.getTeams().add(savedTeam);
      studentRepo.save(member);
    }
    return savedTeam;
  }
}
