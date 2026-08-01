package seal.backend.config;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    rdSpring2.setActiveTime(spr1Active.plusDays(3));
    rdSpring2.setGradingStartTime(spr1Grading.plusDays(3));
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
    // 8. KHỞI TẠO EVENT SUMMER SECOND (BẢN DEMO VÒNG 2)
    // ==========================================
    HackathonEvent eventSummerSecond =
        new HackathonEvent(
            "SEAL Hackathon Summer 2026 Second",
            "AI Agents - Final Demo Edition",
            Duration.ofDays(7),
            EventStatus.FINALIZED,
            summerSeason,
            prizeStructure);
    eventSummerSecond.setTeamsLimit(30);
    eventSummerSecond.setRegistrationStartTime(regStartSummer);
    eventRepo.save(eventSummerSecond);

    Track trSum2A = trackRepo.save(new Track("AI Infrastructure", "Hạ tầng", eventSummerSecond));
    Track trSum2B =
        trackRepo.save(new Track("Data Pipeline & Security", "Dữ liệu", eventSummerSecond));
    Track trSum2C =
        trackRepo.save(new Track("Smart UI/UX & Edge AI", "Giao diện", eventSummerSecond));
    Track[] summer2Tracks = {trSum2A, trSum2B, trSum2C};

    Round rdSum2R1 =
        roundRepo.save(
            new Round(
                "Round 1",
                "Vòng loại",
                Duration.ofMinutes(5),
                Duration.ofMinutes(5),
                eventSummerSecond));
    Round rdSum2R2 =
        roundRepo.save(
            new Round(
                "Round 2",
                "Chung kết",
                Duration.ofMinutes(5),
                Duration.ofMinutes(5),
                eventSummerSecond));

    rdSum2R1.setActiveTime(OffsetDateTime.now().minusHours(2));
    rdSum2R1.setGradingStartTime(OffsetDateTime.now().minusHours(1));
    roundRepo.save(rdSum2R1);

    rdSum2R2.setActiveTime(OffsetDateTime.now().minusMinutes(10));
    rdSum2R2.setGradingStartTime(OffsetDateTime.now().minusMinutes(5));
    roundRepo.save(rdSum2R2);

    for (TemplatedCriteria tc : v1Criterias)
      rdSum2R1
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSum2R1));
    criteriaRepo.saveAll(rdSum2R1.getCriteria());

    for (TemplatedCriteria tc : v2Criterias)
      rdSum2R2
          .getCriteria()
          .add(new Criteria(tc.getName(), tc.getDescription(), tc.getWeight(), rdSum2R2));
    criteriaRepo.saveAll(rdSum2R2.getCriteria());
    roundRepo.saveAll(Arrays.asList(rdSum2R1, rdSum2R2));

    assignMentorAndJudges(trSum2A, Arrays.asList(lNam, lMai, lBao), Arrays.asList(lNgoc, lSon));
    assignMentorAndJudges(trSum2B, Arrays.asList(lNgoc, lSon, lDung), Arrays.asList(lAnh, lHuong));
    assignMentorAndJudges(trSum2C, Arrays.asList(lAnh, lHuong, lTrang), Arrays.asList(lNam, lDung));

    createTeam(
        "Slothub",
        "Hệ thống Agentic RAG tối ưu hóa truy xuất dữ liệu bệnh án điện tử",
        eventSummerSecond,
        trSum2A,
        sXuan,
        sTrung,
        sTriet,
        sDien);
    for (int i = 0; i < 24; i++) {
      createTeam(
          profTeamNames[i],
          "Dự án AI",
          eventSummerSecond,
          summer2Tracks[i % 3],
          generateRealisticStudent(globalStudentCounter++, defaultPwd),
          generateRealisticStudent(globalStudentCounter++, defaultPwd),
          generateRealisticStudent(globalStudentCounter++, defaultPwd));
    }

    // ==========================================
    // 9. XỬ LÝ SUBMISSION, CHẤM ĐIỂM & THĂNG VÒNG (CHO CẢ 3 SỰ KIỆN)
    // ==========================================
    log.info("Bat dau xu ly Submission, Cham diem va Thang vong...");

    // --- 9.1. XỬ LÝ KỲ SPRING 2026 (TOP 2 VÀO VÒNG CHUNG KẾT) ---
    for (Track track : springTracks) {
      List<Team> teamsInTrack = teamRepo.findAllByTrackId(track.getId());
      Map<Team, Double> teamScoresR1 = new java.util.HashMap<>();

      for (Team team : teamsInTrack) {
        Submission subR1 =
            submissionRepo.save(
                new Submission(
                    rdSpring1.getActiveTime().plusMinutes(2),
                    "Giải pháp V1 - " + team.getName(),
                    "Nội dung V1",
                    "https://github.com/seal/" + team.getName().toLowerCase(),
                    "https://youtube.com",
                    "https://docs.google.com",
                    team,
                    rdSpring1));

        double totalScoreR1 = 0;
        int jCount = 0;
        if (!track.getJudges().isEmpty()) {
          for (Lecturer judge : track.getJudges()) {
            if (jCount >= 3) break;
            float baseScore = 6.0f + random.nextInt(3);
            for (Criteria c : rdSpring1.getCriteria()) {
              float jScore = baseScore + (random.nextBoolean() ? 0.5f : 0.0f);
              scoreRepo.save(new Score(c, subR1, judge, jScore));
              totalScoreR1 += (jScore * c.getWeight() / 100.0);
            }
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge)
                    .submission(subR1)
                    .action("GRADED_SUBMISSION")
                    .details("Chấm Vòng 1: " + team.getName())
                    .build());
            jCount++;
          }
        }
        teamScoresR1.put(team, jCount > 0 ? totalScoreR1 / jCount : 0.0);
      }

      List<Team> sortedTeams =
          teamsInTrack.stream()
              .sorted((t1, t2) -> Double.compare(teamScoresR1.get(t2), teamScoresR1.get(t1)))
              .toList();
      for (int i = 0; i < sortedTeams.size(); i++) {
        Team team = sortedTeams.get(i); // Đã bỏ từ khóa 'Team' ở vòng lặp ngoài nên không bị trùng
        if (i < 2) {
          Submission subR2 =
              submissionRepo.save(
                  new Submission(
                      rdSpring2.getActiveTime().plusMinutes(2),
                      "Giải pháp Chung Kết - " + team.getName(),
                      "Nội dung V2",
                      "https://github.com/seal/" + team.getName().toLowerCase(),
                      "https://youtube.com",
                      "https://docs.google.com",
                      team,
                      rdSpring2));
          int jCount2 = 0;
          for (Lecturer judge : track.getJudges()) {
            if (jCount2 >= 3) break;
            float baseR2 = 7.5f + (random.nextInt(2) * 0.5f);
            for (Criteria c : rdSpring2.getCriteria())
              scoreRepo.save(new Score(c, subR2, judge, baseR2));
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge)
                    .submission(subR2)
                    .action("GRADED_SUBMISSION")
                    .details("Chấm Vòng 2: " + team.getName())
                    .build());
            jCount2++;
          }
        } else {
          team.setEliminatedAtRound(rdSpring1);
          teamRepo.save(team);
        }
      }
    }

    // --- 9.2. XỬ LÝ KỲ SUMMER GỐC (GIỮ NGUYÊN DEMO 9 ĐỘI CŨ) ---
    List<Team> summer1Teams = teamRepo.findByHackathonEventId(eventSummer.getId());
    int sumTrA = 0, sumTrB = 0, sumTrC = 0;

    if (rdSum1.getActiveTime() == null) {
      rdSum1.setActiveTime(OffsetDateTime.now());
      roundRepo.save(rdSum1);
    }

    for (Team team : summer1Teams) {
      Track teamTrack = team.getTrack();
      boolean shouldSubmit = false;
      if (team.getName().equals("Slothub")) {
        shouldSubmit = true;
        sumTrA++;
      } else if (teamTrack != null) {
        if (teamTrack.getId().equals(trSumA.getId()) && sumTrA < 3) {
          shouldSubmit = true;
          sumTrA++;
        } else if (teamTrack.getId().equals(trSumB.getId()) && sumTrB < 3) {
          shouldSubmit = true;
          sumTrB++;
        } else if (teamTrack.getId().equals(trSumC.getId()) && sumTrC < 3) {
          shouldSubmit = true;
          sumTrC++;
        }
      }
      if (!shouldSubmit) continue;

      Submission subR1 =
          submissionRepo.save(
              new Submission(
                  rdSum1.getActiveTime().plusMinutes(2),
                  "Giải pháp - " + team.getName(),
                  "Demo V1",
                  "https://github.com",
                  "https://youtube.com",
                  "https://docs.google.com",
                  team,
                  rdSum1));

      if (team.getName().equals("Slothub")
          && teamTrack != null
          && teamTrack.getJudges().size() >= 2) {
        Lecturer j1 = teamTrack.getJudges().get(0), j2 = teamTrack.getJudges().get(1);
        for (Criteria c : rdSum1.getCriteria()) {
          scoreRepo.save(new Score(c, subR1, j1, 8.0f));
          scoreRepo.save(new Score(c, subR1, j2, 8.5f));
        }
        auditLogRepo.save(
            GradingLog.builder()
                .actionTime(OffsetDateTime.now())
                .actor(j1)
                .submission(subR1)
                .action("GRADED_SUBMISSION")
                .details("Chấm Slothub")
                .build());
        auditLogRepo.save(
            GradingLog.builder()
                .actionTime(OffsetDateTime.now())
                .actor(j2)
                .submission(subR1)
                .action("GRADED_SUBMISSION")
                .details("Chấm Slothub")
                .build());
      } else if (teamTrack != null && !teamTrack.getJudges().isEmpty()) {
        int jC = 0;
        for (Lecturer judge : teamTrack.getJudges()) {
          if (jC >= 3) break;
          for (Criteria c : rdSum1.getCriteria())
            scoreRepo.save(new Score(c, subR1, judge, 6.5f + random.nextInt(3) * 0.5f));
          auditLogRepo.save(
              GradingLog.builder()
                  .actionTime(OffsetDateTime.now())
                  .actor(judge)
                  .submission(subR1)
                  .action("GRADED_SUBMISSION")
                  .details("Chấm Vòng 1: " + team.getName())
                  .build());
          jC++;
        }
      }
    }

    // --- 9.3. XỬ LÝ KỲ SUMMER SECOND (TOP 2 VÀO VÒNG 2 + DEMO SLOTHUB R2) ---
    for (Track track : summer2Tracks) {
      List<Team> teamsInTrack = teamRepo.findAllByTrackId(track.getId());
      Map<Team, Double> teamScoresR1 = new java.util.HashMap<>();

      for (Team team : teamsInTrack) {
        Submission subR1 =
            submissionRepo.save(
                new Submission(
                    rdSum2R1.getActiveTime().plusMinutes(2),
                    "Giải pháp V1 - " + team.getName(),
                    "Nội dung V1",
                    "https://github.com",
                    "https://youtube.com",
                    "https://docs.google.com",
                    team,
                    rdSum2R1));

        double totalScoreR1 = 0;
        int jCount = 0;
        if (!track.getJudges().isEmpty()) {
          for (Lecturer judge : track.getJudges()) {
            if (jCount >= 3) break;
            float baseScore = 7.0f + (random.nextInt(3) * 0.5f);
            for (Criteria c : rdSum2R1.getCriteria()) {
              scoreRepo.save(new Score(c, subR1, judge, baseScore));
              totalScoreR1 += (baseScore * c.getWeight() / 100.0);
            }
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(judge)
                    .submission(subR1)
                    .action("GRADED_SUBMISSION")
                    .details("Chấm Vòng 1: " + team.getName())
                    .build());
            jCount++;
          }
        }
        teamScoresR1.put(team, jCount > 0 ? totalScoreR1 / jCount : 0.0);
      }

      List<Team> sortedTeams =
          teamsInTrack.stream()
              .sorted((t1, t2) -> Double.compare(teamScoresR1.get(t2), teamScoresR1.get(t1)))
              .toList();
      for (int i = 0; i < sortedTeams.size(); i++) {
        Team team = sortedTeams.get(i); // Đã sửa triệt để lỗi trùng biến
        if (i < 2) {
          Submission subR2 =
              submissionRepo.save(
                  new Submission(
                      rdSum2R2.getActiveTime().plusMinutes(2),
                      "Giải pháp Chung Kết - " + team.getName(),
                      "Nội dung V2",
                      "https://github.com",
                      "https://youtube.com",
                      "https://docs.google.com",
                      team,
                      rdSum2R2));

          if (team.getName().equals("Slothub") && track.getJudges().size() >= 2) {
            Lecturer j1 = track.getJudges().get(0), j2 = track.getJudges().get(1);
            for (Criteria c : rdSum2R2.getCriteria()) {
              scoreRepo.save(new Score(c, subR2, j1, 8.5f));
              scoreRepo.save(new Score(c, subR2, j2, 9.0f));
            }
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(j1)
                    .submission(subR2)
                    .action("GRADED_SUBMISSION")
                    .details("Chấm Vòng 2 Slothub")
                    .build());
            auditLogRepo.save(
                GradingLog.builder()
                    .actionTime(OffsetDateTime.now())
                    .actor(j2)
                    .submission(subR2)
                    .action("GRADED_SUBMISSION")
                    .details("Chấm Vòng 2 Slothub")
                    .build());
          } else {
            int jCount2 = 0;
            for (Lecturer judge : track.getJudges()) {
              if (jCount2 >= 3) break;
              float baseR2 = 7.5f + (random.nextInt(3) * 0.5f);
              for (Criteria c : rdSum2R2.getCriteria())
                scoreRepo.save(new Score(c, subR2, judge, baseR2));
              auditLogRepo.save(
                  GradingLog.builder()
                      .actionTime(OffsetDateTime.now())
                      .actor(judge)
                      .submission(subR2)
                      .action("GRADED_SUBMISSION")
                      .details("Chấm Vòng 2: " + team.getName())
                      .build());
              jCount2++;
            }
          }
        } else {
          team.setEliminatedAtRound(rdSum2R1);
          teamRepo.save(team);
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
