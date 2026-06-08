package seal.backend.responses;

import seal.backend.entities.User;

public record LoginResponse(String token, User user) {}
