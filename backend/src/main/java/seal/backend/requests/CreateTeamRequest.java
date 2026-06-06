package seal.backend.requests;

import java.util.UUID;

public record CreateTeamRequest(String name, String description, UUID seasonId, UUID leaderId) {}
