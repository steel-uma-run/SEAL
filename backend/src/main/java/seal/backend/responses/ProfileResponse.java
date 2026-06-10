package seal.backend.responses;

import java.util.UUID;
import seal.backend.enums.Role;

public record ProfileResponse(UUID id, String email, String name, Role role) {}
