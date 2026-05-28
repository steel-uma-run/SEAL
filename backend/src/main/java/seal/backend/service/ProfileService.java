package seal.backend.service;

import seal.backend.model.Profile;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class ProfileService {
    private static final Map<String, Profile> mockDatabase = new ConcurrentHashMap<>();

    static {
        Profile testUser = new Profile("hao@fpt.edu.vn", "seal123", "Trương Hoàng Mỹ Xuân", "SE180000", "FPT");
        mockDatabase.put(testUser.getEmail(), testUser);
    }

    public String register(String email, String password, String fullName, String studentCode, String studentType) {
        if (email == null || !email.endsWith(".edu.vn")) {
            return "Lỗi: Định dạng Email phải kết thúc bằng đuôi .edu.vn!";
        }
        if ("FPT".equalsIgnoreCase(studentType) && !email.endsWith("fpt.edu.vn") && !email.endsWith("fe.edu.vn")) {
            return "Lỗi: Hệ FPT yêu cầu sử dụng Email trường FPT cấp!";
        }
        if (password == null || password.length() < 6) {
            return "Lỗi: Mật khẩu tối thiểu phải từ 6 ký tự trở lên!";
        }
        if (mockDatabase.containsKey(email)) {
            return "Email này đã tồn tại trên hệ thống SEAL!";
        }

        Profile newProfile = new Profile(email, password, fullName, studentCode, studentType);
        mockDatabase.put(email, newProfile);
        return "SUCCESS";
    }

    public Profile login(String email, String password) {
        Profile profile = mockDatabase.get(email);
        if (profile != null && profile.getPassword().equals(password)) {
            if ("banned".equals(profile.getStatus())) return null;
            return profile;
        }
        return null;
    }
}