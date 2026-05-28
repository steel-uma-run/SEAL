package seal.backend.controller;

import seal.backend.model.Profile;
import seal.backend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final ProfileService profileService;

    public AuthController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestParam String email,
                                                        @RequestParam String password,
                                                        @RequestParam String fullName,
                                                        @RequestParam String studentCode,
                                                        @RequestParam String studentType) {
        String result = profileService.register(email, password, fullName, studentCode, studentType);
        Map<String, String> response = new HashMap<>();

        if ("SUCCESS".equals(result)) {
            response.put("status", "SUCCESS");
            response.put("message", "Đăng ký tài khoản thành công!");
            return ResponseEntity.ok(response);
        }

        response.put("status", "ERROR");
        response.put("message", result);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        Profile profile = profileService.login(email, password);
        if (profile != null) {
            return ResponseEntity.ok(profile);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Tài khoản hoặc Mật khẩu không chính xác!");
        return ResponseEntity.status(401).body(errorResponse);
    }
}