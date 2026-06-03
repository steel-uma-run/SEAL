package seal.backend.responses;

import seal.backend.enums.Role;

public record ProfileResponse(String email, String name, Role role) {}
