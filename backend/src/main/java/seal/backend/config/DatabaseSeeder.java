package seal.backend.config;

import jakarta.transaction.Transactional;
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
import seal.backend.entities.Coordinator;
import seal.backend.entities.Criteria;
import seal.backend.entities.CriteriaTemplate;
import seal.backend.entities.HackathonEvent;
import seal.backend.entities.Lecturer;
import seal.backend.entities.Round;
import seal.backend.entities.Season;
import seal.backend.entities.Student;
import seal.backend.entities.Team;
import seal.backend.entities.TemplatedCriteria;
import seal.backend.entities.Track;
import seal.backend.enums.EventStatus;
import seal.backend.enums.Role;
import seal.backend.enums.Semester;
import seal.backend.enums.StudentStatus;
import seal.backend.enums.StudentType;
import seal.backend.enums.TeamStatus;
import seal.backend.repositories.CriteriaRepository;
import seal.backend.repositories.CriteriaTemplateRepository;
import seal.backend.repositories.HackathonEventRepository;
import seal.backend.repositories.LecturerRepository;
import seal.backend.repositories.RoundRepository;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;
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

    log.info(
        "Bat dau khoi tao du lieu (Spring 2026: 25 Teams | Summer 2026: 20 Teams + 10 Free"
            + " Students)...");
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

    // Thời gian đăng kí 1/3 - 1/4
    OffsetDateTime regStartSpring =
        OffsetDateTime.of(2026, 3, 1, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    OffsetDateTime regEndSpring = OffsetDateTime.of(2026, 4, 1, 18, 0, 0, 0, ZoneOffset.ofHours(7));

    // Thời gian thi đấu 11/4 - 15/4
    OffsetDateTime startSpring = OffsetDateTime.of(2026, 4, 11, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    OffsetDateTime endSpring = OffsetDateTime.of(2026, 4, 15, 18, 0, 0, 0, ZoneOffset.ofHours(7));
    HackathonEvent eventSpring =
        new HackathonEvent(
            "SEAL Hackathon Spring 2026",
            "Mastering Domain-Specific AI RAG Systems",
            regStartSpring,
            regEndSpring,
            EventStatus.FINALIZED,
            springSeason,
            prizeStructure);
    eventSpring.setTeamsLimit(30);
    eventRepo.save(eventSpring);

    // Thời gian đăng kí 1/6 - 10/7
    OffsetDateTime regStartSummer =
        OffsetDateTime.of(2026, 7, 15, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    OffsetDateTime regEndSummer =
        OffsetDateTime.of(2026, 7, 30, 18, 0, 0, 0, ZoneOffset.ofHours(7));

    // Thời gian thi đấu 15/7 - 30/7
    OffsetDateTime startSummer = OffsetDateTime.of(2026, 7, 15, 7, 0, 0, 0, ZoneOffset.ofHours(7));
    OffsetDateTime endSummer = OffsetDateTime.of(2026, 7, 30, 18, 0, 0, 0, ZoneOffset.ofHours(7));
    HackathonEvent eventSummer =
        new HackathonEvent(
            "SEAL Hackathon Summer 2026",
            "Empowering Enterprise with AI Agents",
            regStartSummer,
            regEndSummer,
            EventStatus.FINALIZED,
            summerSeason,
            prizeStructure);
    eventSummer.setTeamsLimit(30);
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

    // Tracks cho Summer 2026
    Track trSumA =
        trackRepo.save(new Track("AI Infrastructure", "Hạ tầng mô hình ngôn ngữ lớn", eventSummer));
    Track trSumB =
        trackRepo.save(
            new Track("Data Pipeline & Security", "Đường ống dữ liệu và Bảo mật", eventSummer));
    Track trSumC =
        trackRepo.save(
            new Track("Smart UI/UX & Edge AI", "Giao diện thông minh và Edge AI", eventSummer));
    Track[] springTracks = {trSpringA, trSpringB, trSpringC};
    Track[] summerTracks = {trSumA, trSumB, trSumC};

    // Rounds
    Round rdSpring1 =
        roundRepo.save(
            new Round(
                "Round 1",
                startSpring, // round phải sau regis
                startSpring.plusHours(10),
                startSpring.plusHours(11),
                startSpring.plusHours(24),
                startSpring.plusHours(25),
                startSpring.plusHours(48),
                "Vòng loại: Thuyết trình ý tưởng",
                eventSpring));
    Round rdSpring2 =
        roundRepo.save(
            new Round(
                "Round 2",
                startSpring,
                startSpring.plusHours(10),
                startSpring.plusHours(11),
                startSpring.plusHours(24),
                startSpring.plusHours(25),
                startSpring.plusHours(48),
                "Chung kết: The Grand Finale",
                eventSpring));
    Round rdSum1 =
        roundRepo.save(
            new Round(
                "Round 1",
                startSummer,
                startSummer.plusHours(10),
                startSummer.plusHours(11),
                startSummer.plusHours(24),
                startSummer.plusHours(25),
                startSummer.plusHours(48),
                "Vòng Sơ loại RAG",
                eventSummer));
    Round rdSum2 =
        roundRepo.save(
            new Round(
                "Round 2",
                startSummer,
                startSummer.plusHours(10),
                startSummer.plusHours(11),
                startSummer.plusHours(24),
                startSummer.plusHours(25),
                startSummer.plusHours(48),
                "Chung kết Hackathon",
                eventSummer));

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
                new TemplatedCriteria(
                    "Tính đúng đắn & Hoàn thiện chức năng",
                    "Tính đúng đắn & Hoàn thiện chức năng",
                    30,
                    tempV1),
                new TemplatedCriteria(
                    "Ứng dụng AI trong giải pháp", "Ứng dụng AI trong giải pháp", 25, tempV1),
                new TemplatedCriteria(
                    "Thiết kế & Kiến trúc phần mềm", "Thiết kế & Kiến trúc phần mềm", 15, tempV1),
                new TemplatedCriteria(
                    "Thuyết trình & Demo V1", "Thuyết trình & Demo V1", 20, tempV1),
                new TemplatedCriteria(
                    "Teamwork & Tinh thần làm việc", "Teamwork & Tinh thần làm việc", 10, tempV1)));

    List<TemplatedCriteria> v2Criterias =
        templatedCriteriaRepo.saveAll(
            Arrays.asList(
                new TemplatedCriteria(
                    "Độ hoàn thiện & Chất lượng sản phẩm",
                    "Độ hoàn thiện & Chất lượng sản phẩm",
                    25,
                    tempV2),
                new TemplatedCriteria(
                    "Sáng tạo & Khả năng đổi mới", "Sáng tạo & Khả năng đổi mới", 25, tempV2),
                new TemplatedCriteria(
                    "Tính ứng dụng & Khả năng triển khai",
                    "Tính ứng dụng & Khả năng triển khai",
                    20,
                    tempV2),
                new TemplatedCriteria(
                    "Trình bày & Demo sản phẩm V2", "Trình bày & Demo sản phẩm V2", 20, tempV2),
                new TemplatedCriteria(
                    "Làm việc nhóm & Trả lời phản biện",
                    "Làm việc nhóm & Trả lời phản biện",
                    10,
                    tempV2)));

    for (TemplatedCriteria templatedCriteria : v1Criterias) {
      rdSpring1
          .getCriteria()
          .add(
              new Criteria(
                  templatedCriteria.getName(),
                  templatedCriteria.getDescription(),
                  templatedCriteria.getWeight(),
                  rdSpring1));
      criteriaRepo.saveAll(rdSpring1.getCriteria());
    }

    for (TemplatedCriteria templatedCriteria : v2Criterias) {
      rdSpring2
          .getCriteria()
          .add(
              new Criteria(
                  templatedCriteria.getName(),
                  templatedCriteria.getDescription(),
                  templatedCriteria.getWeight(),
                  rdSpring2));
      criteriaRepo.saveAll(rdSpring2.getCriteria());
    }

    for (TemplatedCriteria templatedCriteria : v1Criterias) {
      rdSum1
          .getCriteria()
          .add(
              new Criteria(
                  templatedCriteria.getName(),
                  templatedCriteria.getDescription(),
                  templatedCriteria.getWeight(),
                  rdSum1));
      criteriaRepo.saveAll(rdSum1.getCriteria());
    }

    for (TemplatedCriteria templatedCriteria : v1Criterias) {
      rdSum2
          .getCriteria()
          .add(
              new Criteria(
                  templatedCriteria.getName(),
                  templatedCriteria.getDescription(),
                  templatedCriteria.getWeight(),
                  rdSum2));
      criteriaRepo.saveAll(rdSum2.getCriteria());
    }

    roundRepo.save(rdSpring1);
    roundRepo.save(rdSpring2);
    roundRepo.save(rdSum1);
    roundRepo.save(rdSum2);

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

    Lecturer lChien = createLecturer("Nguyễn Văn Chiến", "chiennv@seal.edu.vn", defaultPwd);
    Lecturer lTri = createLecturer("Phạm Thanh Trí", "tript@seal.edu.vn", defaultPwd);
    Lecturer lSang = createLecturer("Nguyễn Minh Sang", "sangnm@seal.edu.vn", defaultPwd);
    Lecturer lLam = createLecturer("Nguyễn Ngọc Lâm", "lamnn@seal.edu.vn", defaultPwd);
    Lecturer lChi = createLecturer("Lê Thị Quỳnh Chi", "chiltq@seal.edu.vn", defaultPwd);
    Lecturer lThinh = createLecturer("Đỗ Phúc Thịnh", "thinhdp@seal.edu.vn", defaultPwd);
    Lecturer lPhuc = createLecturer("Trần Thiên Phúc", "phuctt@seal.edu.vn", defaultPwd);
    Lecturer lDuong = createLecturer("Phạm Thái Dương", "duongpt@seal.edu.vn", defaultPwd);
    Lecturer lPhuong = createLecturer("Lâm Hữu Khánh Phương", "phuonglhk@seal.edu.vn", defaultPwd);

    // Map Giám khảo
    // Spring Tracks
    assignMentorAndJudges(trSpringA, Arrays.asList(lChien, lTri), Arrays.asList(lSang, lLam));

    assignMentorAndJudges(trSpringB, Arrays.asList(lSang, lLam), Arrays.asList(lChi, lThinh));

    assignMentorAndJudges(trSpringC, Arrays.asList(lChi, lThinh), Arrays.asList(lChien, lTri));

    // Summer Tracks
    assignMentorAndJudges(trSumA, Arrays.asList(lPhuc, lDuong), Arrays.asList(lSang, lLam));
    assignMentorAndJudges(trSumB, Arrays.asList(lChi, lThinh), Arrays.asList(lChien, lTri));
    assignMentorAndJudges(trSumC, Arrays.asList(lLam, lPhuong), Arrays.asList(lChi, lThinh));

    // ==========================================
    // 5. KHỞI TẠO CÁC TEAM CỐ ĐỊNH (CHO VIỆC DEMO CHÍNH)
    // ==========================================
    log.info("Khoi tao cac Team co dinh...");

    Student sXuan =
        createStudent("Trương Hoàng Mỹ Xuân", "xuanthm@fpt.edu.vn", "SE180106", defaultPwd);
    Student sTrung =
        createStudent("Nguyễn Văn Trung", "trungnv@fpt.edu.vn", "SE180107", defaultPwd);
    Student sKhang = createStudent("Trần Tuấn Khang", "khangtt@fpt.edu.vn", "SE180101", defaultPwd);
    Student sTu = createStudent("Võ Minh Tú", "tuvm@fpt.edu.vn", "SE180202", defaultPwd);

    createTeam(
        "Slothub",
        "Hệ thống Agentic RAG tối ưu hóa truy xuất dữ liệu bệnh án điện tử",
        eventSummer,
        trSumA,
        sXuan,
        sTrung,
        sKhang,
        sTu);

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

    log.info("Hoan tat nap du lieu!");
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
    if (judges != null) {
      int judgeCount = 0;
      for (Lecturer j : judges) {
        if (judgeCount < 3) {
          track.getJudges().add(j);
          judgeCount++;
        }
      }
    }

    if (mentors != null) {
      int mentorCount = 0;
      for (Lecturer m : mentors) {
        if (mentorCount < 3) {
          track.getMentors().add(m);
          mentorCount++;
        }
      }
    }

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

  // Tạo Team hỗ trợ n members
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
