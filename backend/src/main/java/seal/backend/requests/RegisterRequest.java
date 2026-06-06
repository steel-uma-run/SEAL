package seal.backend.requests;

public record RegisterRequest(
    String email, String name, String studentId, String password, boolean isExternal) {}
