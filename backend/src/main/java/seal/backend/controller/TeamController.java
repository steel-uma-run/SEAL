package seal.backend.controller;

import seal.backend.model.Team;
import seal.backend.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeam(@RequestParam String email) {
        Team team = teamService.getTeamByEmail(email);
        if (team != null) {
            return ResponseEntity.ok(team);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerTeam(@RequestParam String teamName,
                                                            @RequestParam String trackName,
                                                            @RequestParam String projectIdea,
                                                            @RequestParam String leaderEmail,
                                                            @RequestParam(value = "memberEmail", required = false) List<String> memberEmails,
                                                            @RequestParam(value = "memberName", required = false) List<String> memberNames) {
        String status = teamService.registerNewTeam(teamName, leaderEmail, memberEmails, memberNames, trackName, projectIdea);
        Map<String, String> response = new HashMap<>();

        if ("SUCCESS".equals(status)) {
            response.put("status", "SUCCESS");
            response.put("message", "🚀 Chúc mừng đội đã đăng ký giải đấu SEAL thành công!");
            return ResponseEntity.ok(response);
        }

        response.put("status", "ERROR");
        response.put("message", status);
        return ResponseEntity.badRequest().body(response);
    }
}
