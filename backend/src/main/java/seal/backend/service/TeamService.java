package seal.backend.service;

import seal.backend.model.Team;
import org.springframework.stereotype.Service;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Arrays;

@Service
public class TeamService {
    private static final List<Team> registeredTeams = new CopyOnWriteArrayList<>();

    static {
        List<String> mockEmails = Arrays.asList("trungnt@fpt.edu.vn", "khangvt@fpt.edu.vn");
        List<String> mockNames = Arrays.asList("Nguyễn Thành Trung", "Vũ Tuấn Khang");

        // Đội trưởng là hao@fpt.edu.vn
        Team mockTeam = new Team(
                "SEAL Warriors",
                "hao@fpt.edu.vn",
                mockEmails,
                mockNames,
                "Web Innovation",
                "Hệ thống quản lý giải đấu Hackathon"
        );
        registeredTeams.add(mockTeam);
    }

    public Team getTeamByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String cleanEmail = email.trim();

        // NTT: sửa chỗ này
        for (Team team : registeredTeams) {
            if (team.getLeaderEmail().equalsIgnoreCase(cleanEmail)) return team;
            if (team.getMemberEmails() != null) {
                for (String mEmail : team.getMemberEmails()) {
                    if (mEmail.equalsIgnoreCase(cleanEmail)) return team;
                }
            }
        }
        return null;
    }

    public String registerNewTeam(String teamName, String leaderEmail, List<String> members, List<String> memberNames, String track, String idea) {
        int totalSize = 1 + (members != null ? members.size() : 0);
        if (totalSize < 3 || totalSize > 5) {
            return "Lỗi ràng buộc: Số lượng thành viên của một đội thi bắt buộc phải từ 3 đến 5 người!";
        }
        if (getTeamByEmail(leaderEmail) != null) {
            return "Lỗi: Tài khoản của bạn đã đăng ký hoặc tham gia một đội thi khác!";
        }

        if (members != null) {
            for (String mEmail : members) {
                if (getTeamByEmail(mEmail) != null) {
                    return "Lỗi: Thành viên với Email [" + mEmail + "] đã thuộc về một đội thi khác!";
                }
                if (mEmail.equalsIgnoreCase(leaderEmail)) {
                    return "Lỗi: Email thành viên không được trùng với Đội trưởng!";
                }
            }
        }

        Team newTeam = new Team(teamName, leaderEmail, members, memberNames, track, idea);
        registeredTeams.add(newTeam);
        return "SUCCESS";
    }
}
