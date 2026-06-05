package seal.backend.config;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import seal.backend.entities.Season;
import seal.backend.repositories.SeasonRepository;
import seal.backend.repositories.StudentRepository;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

  private final SeasonRepository seasonRepository;
  private final StudentRepository studentRepository;

  @Override
  public void run(String... args) throws Exception {

    if (seasonRepository.count() == 0 && studentRepository.count() == 0) {

      System.out.println("🌱 ĐANG KHỞI TẠO DỮ LIỆU MẪU (DATA SEEDING)...");

      // 1. Khởi tạo đối tượng Mùa giải (Season)
      Season mockSeason = new Season();
      mockSeason.setStartTime(OffsetDateTime.now());
      mockSeason.setEndTime(OffsetDateTime.now().plusMonths(3));
      mockSeason.setDescription("Mùa giải Hackathon Test Local");
      mockSeason = seasonRepository.save(mockSeason);

      // 2. Tạo leader: tự tạo nha (vì cần token để đăng nhập)
      // nhớ chỉnh status thành ACTIVE nha vì t cấu hình leader là tài khoản ACTIVE thì mới được
      // đăng kí á (thật ra thì t k nhớ t có set chưa hihi)
      // TẠO LEADER XONG THÌ ĐĂNG NHẬP VÔ, LẤY TOKEN ĐỂ ĐI ĐĂNG KÝ TEAM

      System.out.println("✅ ĐÃ TẠO XONG DỮ LIỆU MẪU!");
      System.out.println("👉 COPY 2 ĐOẠN ID NÀY VÀO BODY JSON TRÊN POSTMAN ĐỂ TEST:");
      System.out.println("\"seasonId\": \"" + mockSeason.getId() + "\"");
      System.out.println("------------------------------------------------------");

      // ID CỦA LEADER THÌ TỰ LẤY TỪ DATABASE SAU KHI TẠO LEADER NHA
      // JSON để test create team
      //      {
      //        "name": "Super Coders",
      //              "description": "Team chuyên săn giải Hackathon với dự án AI",
      //              "season_id": "lấy_ở_console",
      //              "leader_id": "lấy_từ_postman_or_sth_else"
      //      }
    }
  }
}
