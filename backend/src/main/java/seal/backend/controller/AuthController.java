package seal.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import seal.backend.enums.StudentType;
import seal.backend.model.User;
import seal.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private ResponseEntity<Map<String, String>> ok;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String studentCode,
            @RequestParam StudentType studentType) {
        String result = userService.register(email, password, fullName, studentCode, studentType);
        Map<String, String> response = new HashMap<>();

        if (result.equals("SUCCESS")) {
            response.put("status", "SUCCESS");
            response.put("message", "Đăng ký tài khoản thành công!");
            return ok;
        }

        response.put("status", "ERROR");
        response.put("message", result);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.login(email, password);
        if (user != null) {
            return ResponseEntity.ok(user);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("message", "Tài khoản hoặc Mật khẩu không chính xác!");
        return ResponseEntity.status(401).body(errorResponse);
    }
}
