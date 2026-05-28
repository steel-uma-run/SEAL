package seal.backend.model;

import java.util.UUID;
import java.util.List;

public class Team {
    private UUID id;
    private String teamName;
    private String leaderEmail;
    private List<String> memberEmails;
    private List<String> memberNames;
    private String trackName;
    private String projectIdea;

    public Team() {
    }

    public Team(String teamName, String leaderEmail, List<String> memberEmails, List<String> memberNames, String trackName, String projectIdea) {
        this.id = UUID.randomUUID();
        this.teamName = teamName;
        this.leaderEmail = leaderEmail;
        this.memberEmails = memberEmails;
        this.memberNames = memberNames;
        this.trackName = trackName;
        this.projectIdea = projectIdea;
    }

    public int getMemberCount() {
        return 1 + (memberEmails != null ? memberEmails.size() : 0);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getLeaderEmail() {
        return leaderEmail;
    }

    public void setLeaderEmail(String leaderEmail) {
        this.leaderEmail = leaderEmail;
    }

    public List<String> getMemberEmails() {
        return memberEmails;
    }

    public void setMemberEmails(List<String> memberEmails) {
        this.memberEmails = memberEmails;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getProjectIdea() {
        return projectIdea;
    }

    public void setProjectIdea(String projectIdea) {
        this.projectIdea = projectIdea;
    }
}