package seal.backend.config;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
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

    // Rounds
    Round rdSpring1 =
        roundRepo.save(
            new Round(
                "Round 1",
                startSpring, // round phải sau regis
                startSpring.plusHours(10),
                "Vòng loại: Thuyết trình ý tưởng",
                eventSpring));
    Round rdSpring2 =
        roundRepo.save(
            new Round(
                "Round 2",
                startSpring.plusDays(1),
                endSpring,
                "Chung kết: The Grand Finale",
                eventSpring));
    Round rdSum1 =
        roundRepo.save(
            new Round(
                "Round 1", startSummer, startSummer.plusHours(8), "Vòng Sơ loại RAG", eventSummer));
    Round rdSum2 =
        roundRepo.save(
            new Round(
                "Round 2",
                startSummer.plusHours(8),
                endSummer,
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
    // 5. TẠO 25 ĐỘI CHO KỲ SPRING 2026
    // ==========================================
    log.info("Khoi tao teams cho SPRING 2026...");
    // Track A
    createTeam(
        "Đẹp trai có gì sai",
        "Phát triển Agentic RAG phục vụ quy trình tuyển dụng tự động",
        eventSpring,
        trSpringA,
        createStudent(
            "Trần Tuấn Khang",
            "khangtt@fpt.edu.vn",
            "SE180101",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Epoch 0",
        "Fine-tuning LLMs nguồn mở trong lĩnh vực Pháp lý",
        eventSpring,
        trSpringA,
        createStudent(
            "Lê Tuấn Kiệt",
            "kietlt@fpt.edu.vn",
            "SE180102",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "food enjoyer",
        "Tối ưu hóa Data Pipeline và xử lý luồng dữ liệu thời gian thực",
        eventSpring,
        trSpringA,
        createStudent(
            "Trần Ngọc Bích",
            "bichtn@fpt.edu.vn",
            "SE180103",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "NEWBIES",
        "Xây dựng Vector Database chuyên dụng phục vụ tra cứu học thuật",
        eventSpring,
        trSpringA,
        createStudent(
            "Phạm Hoàng Long",
            "longph@fpt.edu.vn",
            "SE180104",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "ORTT",
        "Kiến trúc bảo mật chống rò rỉ dữ liệu cho hệ thống doanh nghiệp",
        eventSpring,
        trSpringA,
        createStudent(
            "Đặng Quốc Bảo",
            "baodq@fpt.edu.vn",
            "SE180105",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Slothub",
        "Hệ thống Agentic RAG tối ưu hóa truy xuất dữ liệu bệnh án điện tử",
        eventSpring,
        trSpringA,
        createStudent(
            "Trương Hoàng Mỹ Xuân",
            "xuanthm@fpt.edu.vn",
            "SE180106",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "THE ORCA",
        "Tích hợp DuckDB xử lý truy vấn dữ liệu kinh doanh phân tán",
        eventSpring,
        trSpringA,
        createStudent(
            "Nguyễn Văn Trung",
            "trungnv@fpt.edu.vn",
            "SE180107",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Try",
        "Ứng dụng Retrieval-Augmented Generation trong giáo dục",
        eventSpring,
        trSpringA,
        createStudent(
            "Lý Thảo Vy",
            "vylt@fpt.edu.vn",
            "SE180108",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    // Track B
    createTeam(
        "5 anh em siêu nhân",
        "Automation Test kiểm định phản hồi từ hệ thống AI",
        eventSpring,
        trSpringB,
        createStudent(
            "Phạm Trung Hiếu",
            "hieupt@fpt.edu.vn",
            "SE180201",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "APX",
        "Thiết kế hệ thống báo cáo linh hoạt kết nối dữ liệu bán hàng",
        eventSpring,
        trSpringB,
        createStudent(
            "Võ Minh Tú",
            "tuvm@fpt.edu.vn",
            "SE180202",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Aqua team",
        "Tối ưu hóa Cache memory cho phiên người dùng tương tác LLM",
        eventSpring,
        trSpringB,
        createStudent(
            "Trần Quang Minh",
            "minhtq@hcmut.edu.vn",
            "2110456",
            "ĐH Bách Khoa",
            StudentType.EXTERNAL,
            defaultPwd));
    createTeam(
        "FULI",
        "Đo lường độ trễ của Agent RAG (Performance Testing)",
        eventSpring,
        trSpringB,
        createStudent(
            "Lâm Chấn Huy",
            "huylc@fpt.edu.vn",
            "SE180203",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "NGUHANHSON",
        "UI/UX tối ưu cho nền tảng Dashboard quản trị trí tuệ nhân tạo",
        eventSpring,
        trSpringB,
        createStudent(
            "Đỗ Thanh Sang",
            "sangdt@fpt.edu.vn",
            "SE180204",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "RAGnarok",
        "Trực quan hóa dữ liệu đa chiều (Data Visualization) từ VectorDB",
        eventSpring,
        trSpringB,
        createStudent(
            "Hà Gia Minh",
            "minhhg@fpt.edu.vn",
            "SE180205",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "VAIK",
        "Kịch bản kiểm thử bảo mật lỗ hổng Prompt Injection",
        eventSpring,
        trSpringB,
        createStudent(
            "Đinh Tấn Lộc",
            "locdt@fpt.edu.vn",
            "SE180206",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "WhaleDone",
        "Quản lý CI/CD cho ứng dụng AI phân tán",
        eventSpring,
        trSpringB,
        createStudent(
            "Mai Phương Thảo",
            "thaomp@fpt.edu.vn",
            "SE180207",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    // Track C
    createTeam(
        "404NotFound",
        "Hệ thống Crawler thu thập dữ liệu văn bản chuyên ngành",
        eventSpring,
        trSpringC,
        createStudent(
            "Đinh Gia Hiếu",
            "hieudg@fpt.edu.vn",
            "SE180301",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "BitMindz",
        "Mô hình phân loại ý định (Intent Recognition) trong Chatbot",
        eventSpring,
        trSpringC,
        createStudent(
            "Nguyễn Thành Luân",
            "luannt@fpt.edu.vn",
            "SE180302",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "KQL",
        "Đánh giá rủi ro tín dụng dựa trên xử lý ngôn ngữ tự nhiên",
        eventSpring,
        trSpringC,
        createStudent(
            "Cao Minh Khang",
            "khangcm@fpt.edu.vn",
            "SE180303",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "LearningAgent",
        "Tự động hóa báo cáo tuần thông qua kiến trúc đa Agent",
        eventSpring,
        trSpringC,
        createStudent(
            "Hồ Vĩnh Phát",
            "phathv@fpt.edu.vn",
            "SE180304",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Passion Ducks",
        "Mạng lưới truy xuất thông tin nội bộ cho bộ phận Nhân sự",
        eventSpring,
        trSpringC,
        createStudent(
            "Trịnh Tiểu Cần",
            "cantt@fpt.edu.vn",
            "SE180305",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Red Team Gang",
        "Sử dụng Graph Database mô hình hóa quan hệ tri thức",
        eventSpring,
        trSpringC,
        createStudent(
            "Lê Trần Khánh Hà",
            "haltk@fpt.edu.vn",
            "SE180306",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Underrated",
        "Phát hiện ảo giác (Hallucination) trong các phản hồi của AI",
        eventSpring,
        trSpringC,
        createStudent(
            "Châu Tinh Trì",
            "trict@fpt.edu.vn",
            "SE180307",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "WORKA GANG",
        "Cá nhân hóa nội dung Marketing thông qua phân tích dữ liệu",
        eventSpring,
        trSpringC,
        createStudent(
            "Nguyễn Hải Đăng",
            "dangnh@hcmus.edu.vn",
            "2128005",
            "ĐH KHTN",
            StudentType.EXTERNAL,
            defaultPwd));
    createTeam(
        "YAG",
        "API kết nối linh hoạt hệ thống ERP doanh nghiệp với AI",
        eventSpring,
        trSpringC,
        createStudent(
            "Vương Tấn Kiệt",
            "kietvt@fpt.edu.vn",
            "SE180308",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    // ==========================================
    // 6. GIẢ LẬP 20 ĐỘI THI CHO KỲ SUMMER 2026
    // ==========================================
    log.info("Khoi tao teams cho SUMMER 2026...");
    createTeam(
        "AI Nexus",
        "Nền tảng tích hợp đa mô hình LLM chuyên dụng",
        eventSummer,
        trSumA,
        createStudent(
            "Lý Hải", "hail1@fpt.edu.vn", "SE200401", "Đại học FPT", StudentType.FPT, defaultPwd));
    createTeam(
        "NeuralNet",
        "Tối ưu hóa kiến trúc Transformer cho thiết bị IoT",
        eventSummer,
        trSumA,
        createStudent(
            "Vũ Đình Khoa",
            "khoavd@fpt.edu.vn",
            "SE180402",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "CloudSurfers",
        "Kiến trúc Serverless AI cho ứng dụng tài chính",
        eventSummer,
        trSumA,
        createStudent(
            "Bùi An",
            "anb@hcmut.edu.vn",
            "2210111",
            "ĐH Bách Khoa",
            StudentType.EXTERNAL,
            defaultPwd));
    createTeam(
        "DataCrafters",
        "Hệ thống RAG ứng dụng Hybrid Search",
        eventSummer,
        trSumA,
        createStudent(
            "Đoàn Khắc Việt",
            "vietdk@fpt.edu.vn",
            "SE190404",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "CodePhantoms",
        "Khai phá dữ liệu văn bản với Semantic Routing",
        eventSummer,
        trSumA,
        createStudent(
            "Nguyễn Tân Lập",
            "lapnt@fpt.edu.vn",
            "SE180405",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "LogicBombs",
        "Framework tự động đánh giá chất lượng Prompt",
        eventSummer,
        trSumA,
        createStudent(
            "Lê Viết Ngọc Hoàn",
            "hoanlvn@fpt.edu.vn",
            "SE200406",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "SynthWave",
        "Kiến trúc Multi-Agent phục vụ chăm sóc khách hàng",
        eventSummer,
        trSumA,
        createStudent(
            "Tạ Phương",
            "phuongt@fpt.edu.vn",
            "SE180407",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    createTeam(
        "PromptEngineers",
        "Ngăn chặn Data Leakage trong Data Pipeline của RAG",
        eventSummer,
        trSumB,
        createStudent(
            "Phan Đăng Lưu",
            "luupd@fpt.edu.vn",
            "SE180408",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "RAG Masters",
        "Mã hóa và bảo mật Vector Embeddings",
        eventSummer,
        trSumB,
        createStudent(
            "Cao Văn Hùng",
            "hungcv@fpt.edu.vn",
            "2210155",
            "ĐH Bách Khoa",
            StudentType.EXTERNAL,
            defaultPwd));
    createTeam(
        "NLP Geeks",
        "Xây dựng ETL tự động làm sạch dữ liệu nhiễu",
        eventSummer,
        trSumB,
        createStudent(
            "Đỗ Hà", "had@fpt.edu.vn", "SE180410", "Đại học FPT", StudentType.FPT, defaultPwd));
    createTeam(
        "Visionary Tech",
        "Kiểm định tính ẩn danh của dữ liệu cá nhân (PII)",
        eventSummer,
        trSumB,
        createStudent(
            "Trương Tín",
            "tint@fpt.edu.vn",
            "SE180411",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "MLOps Hub",
        "Pipeline Continuous Training cho mô hình nhúng",
        eventSummer,
        trSumB,
        createStudent(
            "Hứa Minh Kiệt",
            "kiethm@fpt.edu.vn",
            "SE200412",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Agentic Flow",
        "Theo dõi và truy vết (Observability) luồng suy luận Agent",
        eventSummer,
        trSumB,
        createStudent(
            "Đinh Vũ", "vud@hcmus.edu.vn", "2228099", "ĐH KHTN", StudentType.EXTERNAL, defaultPwd));
    createTeam(
        "ByteMe",
        "Giải pháp chống DDOS chuyên biệt cho API AI",
        eventSummer,
        trSumB,
        createStudent(
            "Ngô Bằng",
            "bangn@fpt.edu.vn",
            "SE200414",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    createTeam(
        "Quantum Leap",
        "Tối ưu hóa giao diện trò chuyện Voice-to-Text",
        eventSummer,
        trSumC,
        createStudent(
            "Đào Duy Anh",
            "anhdd@fpt.edu.vn",
            "2128010",
            "ĐH KHTN",
            StudentType.EXTERNAL,
            defaultPwd));
    createTeam(
        "Tech Titans",
        "Chạy mô hình LLM thu nhỏ trực tiếp trên trình duyệt Web",
        eventSummer,
        trSumC,
        createStudent(
            "Lâm Thị Hoàng Oanh",
            "oanhlth@fpt.edu.vn",
            "SE190416",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "CyberCore",
        "Tích hợp AI dự đoán hành vi người dùng trên Mobile",
        eventSummer,
        trSumC,
        createStudent(
            "Chu Nhật Bình",
            "binhcn@fpt.edu.vn",
            "SE190417",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Data Ninjas",
        "Xây dựng Dashboard Real-time giám sát tài nguyên Edge",
        eventSummer,
        trSumC,
        createStudent(
            "Mạc Gia Huy",
            "huymg@fpt.edu.vn",
            "SE190418",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "Edge Computing",
        "Ứng dụng Computer Vision phân tích ảnh tại biên (Edge)",
        eventSummer,
        trSumC,
        createStudent(
            "Cấn Quang Vinh",
            "vinhcq@fpt.edu.vn",
            "SE200419",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));
    createTeam(
        "DevSecOps Pro",
        "Giao diện Copilot hỗ trợ lập trình viên review code",
        eventSummer,
        trSumC,
        createStudent(
            "Tạ Anh Khoa",
            "khoata@fpt.edu.vn",
            "SE190420",
            "Đại học FPT",
            StudentType.FPT,
            defaultPwd));

    // ==========================================
    // 7. TẠO 10 SINH VIÊN "FREE AGENT" CHO DEMO (SUMMER 2026)
    // ==========================================
    log.info("Khoi tao 10 student accounts FREE AGENT...");

    createStudent(
        "Nguyễn Thị Mai",
        "maint@fpt.edu.vn",
        "SE190901",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Lê Công Vinh",
        "vinhlc@fpt.edu.vn",
        "SE200902",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Phạm Bảo Trâm",
        "trampb@fpt.edu.vn",
        "SE190903",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Trần Khắc Hiếu",
        "hieutk@fpt.edu.vn",
        "SE190904",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Vũ Gia Hân", "hanvg@fpt.edu.vn", "SE200905", "Đại học FPT", StudentType.FPT, defaultPwd);
    createStudent(
        "Đặng Tuấn Phong",
        "phongdt@fpt.edu.vn",
        "SE200906",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Ngô Kim Ngân",
        "ngannk@fpt.edu.vn",
        "SE190907",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Lý Anh Tuấn", "tuanla@fpt.edu.vn", "SE180908", "Đại học FPT", StudentType.FPT, defaultPwd);
    createStudent(
        "Bùi Thị Bích Phương",
        "phuongbtb@fpt.edu.vn",
        "SE180909",
        "Đại học FPT",
        StudentType.FPT,
        defaultPwd);
    createStudent(
        "Dương Nhật Minh",
        "minhdn@fpt.edu.vn",
        "2128020",
        "ĐH KHTN",
        StudentType.EXTERNAL,
        defaultPwd);

    log.info("Hoan tat nap du lieu!");
  }

  // ==========================================
  // HELPER METHODS (Giữ nguyên chuẩn Schema)
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

  private Student createStudent(
      String fullName,
      String email,
      String mssv,
      String school,
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
            .schoolName(school)
            .build());
  }

  private Team createTeam(
      String name, String description, HackathonEvent event, Track track, Student leader) {
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
    Team savedTeam = teamRepo.save(team);

    leader.getTeams().add(savedTeam);
    studentRepo.save(leader);

    return savedTeam;
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
}
