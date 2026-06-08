package seal.backend.requests;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;

public record LoginRequest(@Email @Nonnull String email, @Nonnull String password) {}
