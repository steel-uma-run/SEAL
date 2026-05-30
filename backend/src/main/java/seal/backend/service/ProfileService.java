package seal.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import seal.backend.model.Profile;
import seal.backend.model.Status;
import seal.backend.model.StudentType;

@Service
public class ProfileService {
    private static final Map<String, Profile> mockDatabase = new ConcurrentHashMap<>();

    static {
        Profile testUser = new Profile("hao@fpt.edu.vn", "seal123", "Trương Hoàng Mỹ Xuân ăn cức", "SE180000",
                StudentType.FPT);
        mockDatabase.put(testUser.getEmail(), testUser);
    }

    public String register(String email, String password, String fullName, String studentCode,
            StudentType studentType) {
        if (email == null || !email.endsWith(".edu.vn")) {
            return "Lỗi: Định dạng Email phải kết thúc bằng đuôi .edu.vn!";
        }
        if (studentType == StudentType.FPT && !email.endsWith("fpt.edu.vn") && !email.endsWith("fe.edu.vn")) {
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
            if (profile.getStatus() == Status.BANNED) {
                return null;
            }
            return profile;
        }
        return null;
    }
}
